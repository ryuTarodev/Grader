package com.example.grader.service

import com.example.grader.dto.*
import com.example.grader.dto.RequestResponse.*
import com.example.grader.entity.AppUser
import com.example.grader.error.*
import com.example.grader.repository.AppUserRepository
import com.example.grader.security.JwtService
import com.example.grader.util.toAppUserDTO
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

@Service
class AppUserService(
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val s3Service: AwsS3Service
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun signUp(appUserRequest: AppUserRequest): InternalAppUserResponse {
        logger.info("Attempting to sign up user: ${appUserRequest.username}")

        if (appUserRepository.existsAppUserByAppUsername(appUserRequest.username)) {
            logger.warn("Sign up failed: Username already exists - ${appUserRequest.username}")
            throw DuplicateException("Username ${appUserRequest.username} already exists")
        }

        if (appUserRequest.password.isBlank()) {
            logger.warn("Sign up failed: Password is blank for user: ${appUserRequest.username}")
            throw BadRequestException("Password must not be empty")
        }

        val encodedPassword = passwordEncoder.encode(appUserRequest.password)
        val appUser = AppUser(
            appUsername = appUserRequest.username,
            clientPassword = encodedPassword,
            role = appUserRequest.role,
            profilePicture = appUserRequest.profilePicture.orEmpty()
        )

        val savedAppUser = appUserRepository.save(appUser)
        logger.info("User saved successfully: ${savedAppUser.appUsername}")

        val accessToken = jwtService.generateAccessToken(savedAppUser)
        val refreshToken = jwtService.generateRefreshToken(savedAppUser)
        logger.info("Tokens generated for user: ${savedAppUser.appUsername}")

        val appUserDto = savedAppUser.toAppUserDTO()
        if (appUserDto.profilePicture.isNotEmpty()) {
            appUserDto.profilePicture = s3Service.generatePresignedUrl(appUserDto.profilePicture)
        }

        return InternalAppUserResponse(appUser = appUserDto, accessToken = accessToken, refreshToken = refreshToken)
    }

    fun signIn(loginRequest: LoginRequest): InternalAppUserResponse {
        logger.info("User attempting to sign in: ${loginRequest.username}")

        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
            )
        } catch (e: Exception) {
            logger.error("Sign in failed: ${e.message}")
            throw BadCredentialsException("Invalid username or password")
        }

        val savedAppUser = appUserRepository.findAppUserByAppUsername(loginRequest.username)
            ?: throw UserNotFoundException("User not found with username: ${loginRequest.username}")

        val accessToken = jwtService.generateAccessToken(savedAppUser)
        val refreshToken = jwtService.generateRefreshToken(savedAppUser)
        logger.info("Tokens generated for user: ${savedAppUser.appUsername}")

        val appUserDto = savedAppUser.toAppUserDTO()
        if (appUserDto.profilePicture.isNotEmpty()) {
            appUserDto.profilePicture = s3Service.generatePresignedUrl(appUserDto.profilePicture)
        }

        return InternalAppUserResponse(appUser = appUserDto, accessToken = accessToken, refreshToken = refreshToken)
    }


    fun refreshToken(refreshToken: String): InternalAppUserResponse {
        logger.info("Refreshing token: $refreshToken")

        if (!jwtService.isTokenValid(refreshToken)) {
            throw TokenExpiredException("Token is expired")
        }

        val username = jwtService.extractUsername(refreshToken)
        val appUser = appUserRepository.findAppUserByAppUsername(username)
            ?: throw UserNotFoundException("User not found with username: $username")

        val newAccessToken = jwtService.generateAccessToken(appUser)

        return InternalAppUserResponse(
            appUser = appUser.toAppUserDTO(),
            accessToken = newAccessToken,
            refreshToken = refreshToken // reusing old refresh token
        )
    }
    fun resetPassword(resetPasswordRequest: ResetPasswordRequest): AppUserDto {
        logger.info("Password reset attempt for user: ${resetPasswordRequest.username}")

        if (resetPasswordRequest.oldPassword.isBlank() || resetPasswordRequest.newPassword.isBlank()) {
            logger.warn("Reset password failed: one or more fields are empty")
            throw BadRequestException("Password fields must not be empty")
        }

        if (!isValidPassword(resetPasswordRequest.newPassword)) {
            logger.warn("Reset password failed: new password does not meet complexity requirements")
            throw BadRequestException("New password must meet complexity requirements")
        }

        val appUser = appUserRepository.findAppUserByAppUsername(resetPasswordRequest.username)
            ?: throw UserNotFoundException("Username: ${resetPasswordRequest.username} does not exist")

        if (!isPasswordValid(resetPasswordRequest.oldPassword, appUser.clientPassword)) {
            logger.warn("Reset password failed: incorrect old password for user: ${resetPasswordRequest.username}")
            throw UnauthorizedException("Old password is incorrect")
        }

        if (isSamePassword(resetPasswordRequest.newPassword, appUser.clientPassword)) {
            logger.warn("Reset password failed: new password same as old for user: ${resetPasswordRequest.username}")
            throw BadRequestException("New password must be different from the old password")
        }

        appUser.clientPassword = passwordEncoder.encode(resetPasswordRequest.newPassword)
        val savedAppUser = appUserRepository.save(appUser)

        logger.info("Password reset successful for user: ${resetPasswordRequest.username}")
        return savedAppUser.toAppUserDTO()
    }


    fun getAppUsers(): List<AppUserDto> {
        logger.info("Fetching all app users")
        val appUserList = appUserRepository.findAll()

        val dtoList = appUserList.map { user ->
            val dto = user.toAppUserDTO()
            if (dto.profilePicture.isNotEmpty()) {
                dto.profilePicture = s3Service.generatePresignedUrl(user.username).toString()
                logger.info("Presigned URL generated for user: ${user.username}")
            }
            dto
        }

        logger.info("Retrieved ${dtoList.size} AppUsers successfully")
        return dtoList
    }

    fun getUserById(userId: Long): AppUserDto {
        logger.info("Fetching user by ID: $userId")

        val appUser = appUserRepository.findById(userId).orElseThrow {
            logger.warn("No user found with ID: $userId")
            UserNotFoundException("No User found with ID $userId")
        }

        val appUserDto = appUser.toAppUserDTO()
        if (appUser.profilePicture.isNotEmpty()) {
            val presignedUrl = s3Service.generatePresignedUrl(appUser.profilePicture)
            appUserDto.profilePicture = presignedUrl
            logger.info("Presigned URL generated for profile picture of user ID: $userId")
        }

        return appUserDto
    }

    fun uploadUserProfile(userId: Long, png: MultipartFile): AppUserDto {
        logger.info("Uploading profile picture for user ID: $userId")

        val pngName = s3Service.savePngToS3(png)
        val appUser = appUserRepository.findById(userId).orElseThrow {
            logger.warn("Upload failed: No user found with ID $userId")
            UserNotFoundException("No User found with ID $userId")
        }

        appUser.profilePicture = pngName
        appUserRepository.save(appUser)

        logger.info("Profile picture uploaded for user: ${appUser.appUsername}")
        return appUser.toAppUserDTO()
    }

    fun updateUser(userId: Long, updateRequest: AppUserRequest): AppUserDto {
        logger.info("Updating user with ID: $userId")

        val appUser = appUserRepository.findById(userId).orElseThrow {
            logger.warn("Update failed: No user found with ID $userId")
            UserNotFoundException("No User found with ID $userId")
        }

        updateRequest.username.takeIf { it.isNotBlank() }?.let { newUsername ->
            if (appUserRepository.existsAppUserByAppUsername(newUsername) && newUsername != appUser.appUsername) {
                logger.warn("Update failed: Username $newUsername already taken")
                throw DuplicateException("Username $newUsername is already taken")
            }
            appUser.appUsername = newUsername
        }

        updateRequest.role.let { appUser.role = it }
        updateRequest.profilePicture?.let { appUser.profilePicture = it }
        appUser.updatedAt = Instant.now()

        val updatedUser = appUserRepository.save(appUser)
        logger.info("User with ID: $userId updated successfully")
        return updatedUser.toAppUserDTO()
    }

    fun deleteUser(userId: Long) {
        logger.info("Attempting to delete user with ID: $userId")

        val existingAppUser = appUserRepository.findById(userId).orElseThrow {
            logger.warn("Delete failed: No user found with ID $userId")
            UserNotFoundException("No User found with ID $userId")
        }

        appUserRepository.delete(existingAppUser)
        logger.info("User deleted successfully: ${existingAppUser.appUsername}")
    }

    private fun isPasswordValid(inputPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(inputPassword, encodedPassword)
    }

    private fun isSamePassword(newPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(newPassword, encodedPassword)
    }

    private fun isValidPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}\$")
        return regex.matches(password)
    }
}