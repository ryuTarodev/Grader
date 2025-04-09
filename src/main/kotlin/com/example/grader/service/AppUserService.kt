package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.AppUserDto
import com.example.grader.dto.LoginRequest
import com.example.grader.dto.LoginResponse
import com.example.grader.entity.AppUser
import com.example.grader.error.UserNotFoundException
import com.example.grader.repository.AppUserRepository
import com.example.grader.security.JwtService
import com.example.grader.util.ResponseUtil
import com.example.grader.util.mapUserListEntityToUserListDTO
import com.example.grader.util.toAppUserDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

import kotlin.random.Random

@Service
class AppUserService(
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val clientSessionService: ClientSessionService,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val s3Service: AwsS3Service

) {
    @Value("\${spring.cloud.aws.s3.bucketName}")
    private lateinit var bucketName: String

    private val logger = LoggerFactory.getLogger(javaClass)



    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
    }

    fun signUp(appUser: AppUser): ApiResponse<AppUserDto> {
        return try {
            if (appUserRepository.existsAppUserByAppUsername(appUser.appUsername)) {
                throw IllegalArgumentException("${appUser.appUsername} already exists")
            }

            logger.info("Saving user with username: ${appUser.appUsername}")

            appUser.clientPassword = passwordEncoder.encode(appUser.clientPassword)
            val savedAppUser = appUserRepository.save(appUser)
            val appUserDTO = savedAppUser.toAppUserDTO()

            ResponseUtil.created(
                message = "User created successfully",
                data = appUserDTO,
                metadata = null
            )

        } catch (e: IllegalArgumentException) {
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

    fun resetPassword(loginRequest: LoginRequest, newPassword: String): ApiResponse<AppUserDto> {
        return try {
            val appUser =appUserRepository.findAppUserByAppUsername(loginRequest.username)
            ?: throw UserNotFoundException("Username: ${loginRequest.username} does not exist!")
            appUser.clientPassword = passwordEncoder.encode(newPassword)

            val savedAppUser = appUserRepository.save(appUser)
            val appUserDTO = savedAppUser.toAppUserDTO()

            ResponseUtil.created(
                message = "User change password successfully",
                data = appUserDTO,
                metadata = null
            )
        } catch (e: UserNotFoundException) {
            logger.error("UserNotFound: ${e.message}")

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

    fun signIn(loginRequest: LoginRequest): ApiResponse<AppUserDto> {
        return try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password))
            val appUser = appUserRepository.findAppUserByAppUsername(loginRequest.username)
                ?: throw UserNotFoundException("User not found with username: ${loginRequest.username}")
            val accessToken = jwtService.generateAccessToken(appUser)
            val appUserDTO = appUser.toAppUserDTO()
            logger.info("$appUserDTO login successful!")
            val loginResponse = LoginResponse(
                expirationTime = "1 Days",
                accessToken = accessToken,
            )
            ResponseUtil.success("User login successfully", appUserDTO, loginResponse)
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
            val appUserDtoList = mapUserListEntityToUserListDTO(appUserList)
            logger.info("Find AppUsers successfully")
            ResponseUtil.success(
                message = "Find AppUsers successfully",
                data = appUserDtoList,
                metadata = null,
            )
        }catch (e: UserNotFoundException){
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
}

