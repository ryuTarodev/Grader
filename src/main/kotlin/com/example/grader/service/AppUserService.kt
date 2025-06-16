package com.example.grader.service

import com.example.grader.dto.*
import com.example.grader.dto.RequesttResponse.LoginRequest
import com.example.grader.dto.RequesttResponse.LoginResponse
import com.example.grader.dto.RequesttResponse.ResetPasswordRequest
import com.example.grader.dto.RequesttResponse.AppUserRequest
import com.example.grader.entity.AppUser
import com.example.grader.error.BadRequestException
import com.example.grader.error.UserNotFoundException
import com.example.grader.error.DuplicateException
import com.example.grader.error.UnauthorizedException
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

    fun signUp(appUserRequest: AppUserRequest): AppUserDto {
        if (appUserRepository.existsAppUserByAppUsername(appUserRequest.appUsername)) {
            throw DuplicateException("Username ${appUserRequest.appUsername} already exists")
        }

        logger.info("Saving user with username: ${appUserRequest.appUsername}")
        val appUser = AppUser(
            appUsername = appUserRequest.appUsername,
            clientPassword = passwordEncoder.encode(appUserRequest.clientPassword),
            role = appUserRequest.role,
        )
        val savedAppUser = appUserRepository.save(appUser)

        return savedAppUser.toAppUserDTO()
    }

    fun resetPassword(resetPasswordRequest: ResetPasswordRequest): AppUserDto {
        // Validate input
        if (resetPasswordRequest.oldPassword.isBlank() || resetPasswordRequest.newPassword.isBlank()) {
            throw BadRequestException("Password fields must not be empty")
        }

        if (!isValidPassword(resetPasswordRequest.newPassword)) {
            throw BadRequestException("New password must meet complexity requirements")
        }

        val appUser = appUserRepository.findAppUserByAppUsername(resetPasswordRequest.username)
            ?: throw UserNotFoundException("Username: ${resetPasswordRequest.username} does not exist")

        // Validate old password
        if (!isPasswordValid(resetPasswordRequest.oldPassword, appUser.clientPassword)) {
            throw UnauthorizedException("Old password is incorrect")
        }

        // Prevent password reuse
        if (isSamePassword(resetPasswordRequest.newPassword, appUser.clientPassword)) {
            throw BadRequestException("New password must be different from the old password")
        }

        // Update password
        appUser.clientPassword = passwordEncoder.encode(resetPasswordRequest.newPassword)
        val savedAppUser = appUserRepository.save(appUser)

        return savedAppUser.toAppUserDTO()
    }

    fun signIn(loginRequest: LoginRequest): Pair<AppUserDto, LoginResponse> {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.username,
                loginRequest.password
            )
        )

        val appUser = appUserRepository.findAppUserByAppUsername(loginRequest.username)
            ?: throw UserNotFoundException("User not found with username: ${loginRequest.username}")

        val accessToken = jwtService.generateAccessToken(appUser)

        logger.info("User ${appUser.appUsername} login successful!")

        val loginResponse = LoginResponse(
            expirationTime = "1 Days",
            accessToken = accessToken,
        )

        return Pair(appUser.toAppUserDTO(), loginResponse)
    }

    fun getAppUsers(): List<AppUserDto> {
        val appUserList = appUserRepository.findAll()
        logger.info("Find AppUsers successfully")
        return mapUserListEntityToUserListDTO(appUserList)
    }

    fun getUserById(userId: Long): AppUserDto {
        val appUser = appUserRepository.findById(userId).orElseThrow {
            UserNotFoundException("No User found with ID $userId")
        }

        val appUserDto = appUser.toAppUserDTO()
        if (appUser.profilePicture.isNotEmpty()) {
            val presignedUrl = s3Service.generatePresignedUrl(appUser.profilePicture)
            appUserDto.profilePicture = presignedUrl
        }

        return appUserDto
    }

    fun uploadUserProfile(userId: Long, png: MultipartFile): AppUserDto {
        val pngName = s3Service.savePngToS3(png)
        val appUser = appUserRepository.findById(userId).orElseThrow {
            UserNotFoundException("No User found with ID $userId")
        }

        appUser.profilePicture = pngName
        appUserRepository.save(appUser)

        return appUser.toAppUserDTO()
    }

    fun updateUser(userId: Long, updateRequest: AppUserRequest): AppUserDto {
        val appUser = appUserRepository.findById(userId).orElseThrow {
            UserNotFoundException("No User found with ID $userId")
        }

        updateRequest.appUsername.takeIf { it.isNotBlank() }?.let { newUsername ->
            if (appUserRepository.existsAppUserByAppUsername(newUsername) && newUsername != appUser.appUsername) {
                throw DuplicateException("Username $newUsername is already taken")
            }
            appUser.appUsername = newUsername
        }

        updateRequest.role.let { appUser.role = it }
        updateRequest.profilePicture?.let { appUser.profilePicture = it }
        appUser.updatedAt = Instant.now()

        val updatedUser = appUserRepository.save(appUser)
        return updatedUser.toAppUserDTO()
    }

    fun deleteUser(userId: Long) {
        val existingAppUser = appUserRepository.findById(userId).orElseThrow {
            UserNotFoundException("No User found with ID $userId")
        }

        appUserRepository.delete(existingAppUser)
    }

    // Private helper methods
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