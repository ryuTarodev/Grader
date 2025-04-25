package com.example.grader.service

import com.example.grader.dto.*
import com.example.grader.dto.RequstResponse.LoginRequest
import com.example.grader.dto.RequstResponse.LoginResponse
import com.example.grader.dto.RequstResponse.ResetPasswordRequest
import com.example.grader.dto.RequstResponse.UserRequest
import com.example.grader.entity.AppUser
import com.example.grader.error.UserNotFoundException
import com.example.grader.repository.AppUserRepository
import com.example.grader.security.JwtService
import com.example.grader.util.ResponseUtil
import com.example.grader.util.mapUserListEntityToUserListDTO
import com.example.grader.util.toAppUserDTO
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Instant


@Service
class AppUserService(
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val clientSessionService: ClientSessionService,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val s3Service: AwsS3Service

) {


    private val logger = LoggerFactory.getLogger(javaClass)

    fun signUp(appUser: AppUser): ApiResponse<AppUserDto> {
        return try {
            if (appUserRepository.existsAppUserByAppUsername(appUser.appUsername)) {
                throw IllegalArgumentException("${appUser.appUsername} already exists")
            }

            logger.info("Saving user with username: ${appUser.appUsername}")

            appUser.clientPassword = passwordEncoder.encode(appUser.clientPassword)
            val savedAppUser = appUserRepository.save(appUser)

            ResponseUtil.created(
                message = "User created successfully",
                data = savedAppUser.toAppUserDTO(),
                metadata = null
            )

        } catch (e: IllegalArgumentException) {
            logger.error("Unauthorized: ${e.message}")
            ResponseUtil.unauthorized(
                message = "Invalid sign-up: ${e.message}",
                data = AppUserDto()
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = AppUserDto()
            )
        }
    }

    fun resetPassword(resetPasswordRequest: ResetPasswordRequest): ApiResponse<AppUserDto> {
        return try {
            val appUser = appUserRepository.findAppUserByAppUsername(resetPasswordRequest.username)
                ?: throw UserNotFoundException("Username: ${resetPasswordRequest.username} does not exist!")

            if (!passwordEncoder.matches(resetPasswordRequest.oldPassword, appUser.clientPassword)) {
                return ResponseUtil.unauthorized(
                    message = "Old password is incorrect.",
                    data = AppUserDto()
                )
            }

            if (passwordEncoder.matches(resetPasswordRequest.newPassword, appUser.clientPassword)) {
                return ResponseUtil.badRequest(
                    message = "New password must be different from the old password.",
                    data = AppUserDto()
                )
            }

            appUser.clientPassword = passwordEncoder.encode(resetPasswordRequest.newPassword)

            val savedAppUser = appUserRepository.save(appUser)


            ResponseUtil.created(
                message = "User changed password successfully",
                data = savedAppUser.toAppUserDTO(),
                metadata = null
            )
        } catch (e: UserNotFoundException) {
            logger.error("UserNotFound: ${e.message}")
            ResponseUtil.notFound(
                message = "Invalid request: ${e.message}",
                data = AppUserDto()
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = AppUserDto()
            )
        }
    }

    fun signIn(loginRequest: LoginRequest): ApiResponse<AppUserDto> {
        return try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password))
            val appUser = appUserRepository.findAppUserByAppUsername(loginRequest.username)
                ?: throw UserNotFoundException("User not found with username: ${loginRequest.username}")
            val accessToken = jwtService.generateAccessToken(appUser)

            logger.info("$appUser login successful!")
            val loginResponse = LoginResponse(
                expirationTime = "1 Days",
                accessToken = accessToken,
            )
            ResponseUtil.success("User login successfully", appUser.toAppUserDTO(), loginResponse)
        } catch (e: UserNotFoundException) {
            logger.error("User not found: ${e.message}")
            ResponseUtil.notFound(
                message = "Invalid sign-up: ${e.message}",
                data = AppUserDto()
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = AppUserDto()
            )
        }
    }

    fun getAppUsers(): ApiResponse<List<AppUserDto>> {
        return try {
            val appUserList = appUserRepository.findAll() ?: throw UserNotFoundException("No User Initialize")
            logger.info("Find AppUsers successfully")
            ResponseUtil.success(
                message = "Find AppUsers successfully",
                data = mapUserListEntityToUserListDTO(appUserList),
                metadata = null,
            )
        }catch (e: UserNotFoundException){
            logger.error("UserNotFound: ${e.message}")
            ResponseUtil.notFound(
                message = "Invalid AppUsers",
                data = emptyList()
            )

        }catch (e: Exception) {
            logger.error("An unexpected error occurred: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = emptyList()
            )
        }
    }

    fun getUserById(userId: Long): ApiResponse<AppUserDto> {
        return try {
            val appUser = appUserRepository.findById(userId).orElseThrow {
                UserNotFoundException("No User found with ID $userId")
            }
            val appUserDto = appUser.toAppUserDTO()
            if (appUser.profilePicture.isNotEmpty()) {
                val presignedUrl = s3Service.generatePresignedUrl(appUser.profilePicture)
                appUserDto.profilePicture = presignedUrl
            }
            ResponseUtil.success(
                message = "Find AppUser successfully",
                data = appUserDto,
                metadata = null
            )
        }catch (e: UserNotFoundException){
            logger.error("UserNotFound: ${e.message}")
            ResponseUtil.notFound(
                message = "Invalid AppUsers",
                data = AppUserDto()
            )
        }catch (e: Exception) {
            logger.error("An unexpected error occurred: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = AppUserDto()
            )
        }
    }


    fun uploadUserProfile(userId: Long, png: MultipartFile): ApiResponse<AppUserDto> {
        return try {
            val pngName = s3Service.savePngToS3(png)
            val appUser = appUserRepository.findById(userId).orElseThrow {
                UserNotFoundException("No User found with ID $userId")
            }
            appUser.profilePicture = pngName
            appUserRepository.save(appUser)
            val appUserDto = appUser.toAppUserDTO()
            ResponseUtil.success(
                message = "Upload $pngName to $userId successfully",
                data = appUserDto,
                metadata = null
            )
        }catch (e: UserNotFoundException){
            logger.error("UserNotFound: ${e.message}")
            ResponseUtil.notFound(
                message = "Invalid AppUsers",
                data = AppUserDto()
            )
        }catch (e: Exception) {
            logger.error("An unexpected error occurred: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = AppUserDto()
            )
        }

    }
    fun updateUser(userId: Long, updateRequest: UserRequest): ApiResponse<AppUserDto> {
        return try {
            val appUser = appUserRepository.findById(userId).orElseThrow {
                UserNotFoundException("No User found with ID $userId")
            }

            updateRequest.appUsername.takeIf { it.isNotBlank() }?.let { newUsername ->
                if (appUserRepository.existsAppUserByAppUsername(newUsername) && newUsername != appUser.appUsername) {
                    throw IllegalArgumentException("Username $newUsername is already taken.")
                }
                appUser.appUsername = newUsername
            }

            updateRequest.role.let { appUser.role = it }

            updateRequest.profilePicture.let { appUser.profilePicture = it }

            appUser.updatedAt = Instant.now()

            val updatedUser = appUserRepository.save(appUser)


            ResponseUtil.success(
                message = "User updated successfully",
                data = updatedUser.toAppUserDTO(),
                metadata = null
            )
        } catch (e: UserNotFoundException) {
            logger.error("UserNotFound: ${e.message}")
            ResponseUtil.notFound(
                message = "Invalid AppUser: ${e.message}",
                data = AppUserDto()
            )
        } catch (e: IllegalArgumentException) {
            logger.error("BadRequest: ${e.message}")
            ResponseUtil.badRequest(
                message = "Update failed: ${e.message}",
                data = AppUserDto()
            )
        } catch (e: Exception) {
            logger.error("Unexpected error: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = AppUserDto()
            )
        }
    }

    fun deleteUser(userId: Long): ApiResponse<Unit> {
        return try {
            val existingAppUser = appUserRepository.findById(userId).orElseThrow {
                UserNotFoundException("No User found with ID $userId")
            }

            appUserRepository.delete(existingAppUser)

            ResponseUtil.success(
                message = "User deleted successfully",
                data = Unit,
                metadata = null
            )
        } catch (e: UserNotFoundException) {
            logger.error("UserNotFound: ${e.message}")
            ResponseUtil.notFound(
                message = "Invalid AppUsers: ${e.message}",
                data = Unit
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = Unit
            )
        }
    }
}

