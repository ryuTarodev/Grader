package com.example.grader.service

import com.example.grader.dto.*
import com.example.grader.dto.RequstResponse.LoginRequest
import com.example.grader.dto.RequstResponse.LoginResponse
import com.example.grader.dto.RequstResponse.ResetPasswordRequest
import com.example.grader.dto.RequstResponse.AppUserRequest
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

    companion object {
        private val EMPTY_APPUSER = AppUserDto()
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    fun signUp(appUser: AppUser): ApiResponse<AppUserDto> {
        if (appUserRepository.existsAppUserByAppUsername(appUser.appUsername)) {
            throw IllegalArgumentException("${appUser.appUsername} already exists")
        }

        logger.info("Saving user with username: ${appUser.appUsername}")

        appUser.clientPassword = passwordEncoder.encode(appUser.clientPassword)
        val savedAppUser = appUserRepository.save(appUser)

        return ResponseUtil.created(
            message = "User created successfully",
            data = savedAppUser.toAppUserDTO(),
            metadata = null
        )
    }

    fun resetPassword(resetPasswordRequest: ResetPasswordRequest): ApiResponse<AppUserDto> {
        // Validate input
        if (resetPasswordRequest.oldPassword.isBlank() || resetPasswordRequest.newPassword.isBlank()) {
            return ResponseUtil.badRequest(
                message = "Password fields must not be empty.",
                data = EMPTY_APPUSER
            )
        }

        if (!isValidPassword(resetPasswordRequest.newPassword)) {
            return ResponseUtil.badRequest(
                message = "New password must meet complexity requirements.",
                data = EMPTY_APPUSER
            )
        }

        // Find user
        val appUser = appUserRepository.findAppUserByAppUsername(resetPasswordRequest.username)
            ?: throw UserNotFoundException("Username: ${resetPasswordRequest.username} does not exist!")

        // Check old password
        if (!isPasswordValid(resetPasswordRequest.oldPassword, appUser.clientPassword)) {
            return ResponseUtil.unauthorized(
                message = "Old password is incorrect.",
                data = EMPTY_APPUSER
            )
        }

        // Prevent reusing the same password
        if (isSamePassword(resetPasswordRequest.newPassword, appUser.clientPassword)) {
            return ResponseUtil.badRequest(
                message = "New password must be different from the old password.",
                data = EMPTY_APPUSER
            )
        }

        // Update password
        appUser.clientPassword = passwordEncoder.encode(resetPasswordRequest.newPassword)

        val savedAppUser = appUserRepository.save(appUser)

        return ResponseUtil.created(
            message = "User changed password successfully",
            data = savedAppUser.toAppUserDTO(),
            metadata = null
        )

    }


    private fun isPasswordValid(inputPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(inputPassword, encodedPassword)
    }

    private fun isSamePassword(newPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(newPassword, encodedPassword)
    }

    private fun isValidPassword(password: String): Boolean {
        // Customize rules as needed: length ≥ 8, at least 1 digit, 1 uppercase, 1 lowercase, 1 symbol
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}\$")
        return regex.matches(password)
    }

    fun signIn(loginRequest: LoginRequest): ApiResponse<AppUserDto> {

        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.username,
                loginRequest.password
            )
        )
        val appUser = appUserRepository.findAppUserByAppUsername(loginRequest.username)
            ?: throw UserNotFoundException("User not found with username: ${loginRequest.username}")
        val accessToken = jwtService.generateAccessToken(appUser)

        logger.info("$appUser login successful!")
        val loginResponse = LoginResponse(
            expirationTime = "1 Days",
            accessToken = accessToken,
        )
        return ResponseUtil.success("User login successfully", appUser.toAppUserDTO(), loginResponse)

    }

    fun getAppUsers(): ApiResponse<List<AppUserDto>> {
        val appUserList = appUserRepository.findAll()
        logger.info("Find AppUsers successfully")
        return ResponseUtil.success(
            message = "Find AppUsers successfully",
            data = mapUserListEntityToUserListDTO(appUserList),
            metadata = null,
        )

    }

    fun getUserById(userId: Long): ApiResponse<AppUserDto> {

        val appUser = appUserRepository.findById(userId).orElseThrow {
            UserNotFoundException("No User found with ID $userId")
        }
        val appUserDto = appUser.toAppUserDTO()
        if (appUser.profilePicture.isNotEmpty()) {
            val presignedUrl = s3Service.generatePresignedUrl(appUser.profilePicture)
            appUserDto.profilePicture = presignedUrl
        }
        return ResponseUtil.success(
            message = "Find AppUser successfully",
            data = appUserDto,
            metadata = null
        )

    }


    fun uploadUserProfile(userId: Long, png: MultipartFile): ApiResponse<AppUserDto> {

        val pngName = s3Service.savePngToS3(png)
        val appUser = appUserRepository.findById(userId).orElseThrow {
            UserNotFoundException("No User found with ID $userId")
        }
        appUser.profilePicture = pngName
        appUserRepository.save(appUser)
        val appUserDto = appUser.toAppUserDTO()
        return ResponseUtil.success(
            message = "Upload $pngName to $userId successfully",
            data = appUserDto,
            metadata = null
        )


    }

    fun updateUser(userId: Long, updateRequest: AppUserRequest): ApiResponse<AppUserDto> {

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


        return ResponseUtil.success(
            message = "User updated successfully",
            data = updatedUser.toAppUserDTO(),
            metadata = null
        )

    }

    fun deleteUser(userId: Long): ApiResponse<Unit> {

        val existingAppUser = appUserRepository.findById(userId).orElseThrow {
            UserNotFoundException("No User found with ID $userId")
        }

        appUserRepository.delete(existingAppUser)

        return ResponseUtil.success(
            message = "User deleted successfully",
            data = Unit,
            metadata = null
        )

    }
}

