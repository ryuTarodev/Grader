package com.example.grader.service

import com.example.grader.dto.*
import com.example.grader.dto.RequestResponse.*
import com.example.grader.entity.AppUser
import com.example.grader.error.BadRequestException
import com.example.grader.error.UserNotFoundException
import com.example.grader.error.DuplicateException
import com.example.grader.error.UnauthorizedException
import com.example.grader.repository.AppUserRepository
import com.example.grader.security.JwtService
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

    fun signUp(appUserRequest: AppUserRequest): AppUserResponse {
        if (appUserRepository.existsAppUserByAppUsername(appUserRequest.username)) {
            throw DuplicateException("Username ${appUserRequest.username} already exists")
        }

        logger.info("Saving user with username: ${appUserRequest.username}")

        if (appUserRequest.password.isBlank()) {
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
        val accessToken = jwtService.generateAccessToken(savedAppUser)

        val appUserDto = savedAppUser.toAppUserDTO()
        if (appUserDto.profilePicture.isNotEmpty()) {
            appUserDto.profilePicture = s3Service.generatePresignedUrl(appUserDto.profilePicture)
        }

        return AppUserResponse(appUser = appUserDto, token = accessToken)
    }

    fun resetPassword(resetPasswordRequest: ResetPasswordRequest): AppUserDto {
        if (resetPasswordRequest.oldPassword.isBlank() || resetPasswordRequest.newPassword.isBlank()) {
            throw BadRequestException("Password fields must not be empty")
        }

        if (!isValidPassword(resetPasswordRequest.newPassword)) {
            throw BadRequestException("New password must meet complexity requirements")
        }

        val appUser = appUserRepository.findAppUserByAppUsername(resetPasswordRequest.username)
            ?: throw UserNotFoundException("Username: ${resetPasswordRequest.username} does not exist")

        if (!isPasswordValid(resetPasswordRequest.oldPassword, appUser.clientPassword)) {
            throw UnauthorizedException("Old password is incorrect")
        }

        if (isSamePassword(resetPasswordRequest.newPassword, appUser.clientPassword)) {
            throw BadRequestException("New password must be different from the old password")
        }

        appUser.clientPassword = passwordEncoder.encode(resetPasswordRequest.newPassword)
        val savedAppUser = appUserRepository.save(appUser)

        return savedAppUser.toAppUserDTO()
    }


    //      verifies the username and password.
    fun signIn(loginRequest: LoginRequest): AppUserResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.username,
                loginRequest.password
            )
        )

        val savedAppUser = appUserRepository.findAppUserByAppUsername(loginRequest.username)
            ?: throw UserNotFoundException("User not found with username: ${loginRequest.username}")

        val accessToken = jwtService.generateAccessToken(savedAppUser)

        logger.info("User ${savedAppUser.appUsername} login successful!")

        val appUserDto = savedAppUser.toAppUserDTO()
        if (appUserDto.profilePicture.isNotEmpty()) {
            appUserDto.profilePicture = s3Service.generatePresignedUrl(appUserDto.profilePicture)
        }

        return AppUserResponse(appUser = appUserDto, token = accessToken)
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

        updateRequest.username.takeIf { it.isNotBlank() }?.let { newUsername ->
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