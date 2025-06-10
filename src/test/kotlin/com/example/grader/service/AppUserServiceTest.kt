package com.example.grader.service

import com.example.grader.dto.RequstResponse.*
import com.example.grader.dto.AppUserDto
import com.example.grader.entity.AppUser
import com.example.grader.error.UserNotFoundException
import com.example.grader.repository.AppUserRepository
import com.example.grader.security.JwtService
import com.example.grader.util.ResponseUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.multipart.MultipartFile
import java.util.*

class AppUserServiceTest {

    private lateinit var appUserRepository: AppUserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var jwtService: JwtService
    private lateinit var s3Service: AwsS3Service
    private lateinit var clientSessionService: ClientSessionService
    private lateinit var appUserService: AppUserService

    @BeforeEach
    fun setup() {
        appUserRepository = mock(AppUserRepository::class.java)
        passwordEncoder = mock(PasswordEncoder::class.java)
        authenticationManager = mock(AuthenticationManager::class.java)
        jwtService = mock(JwtService::class.java)
        s3Service = mock(AwsS3Service::class.java)
        clientSessionService = mock(ClientSessionService::class.java)
        appUserService = AppUserService(
            appUserRepository,
            passwordEncoder,
            clientSessionService,
            authenticationManager,
            jwtService,
            s3Service
        )
    }

    @Test
    fun `signUp should create user if username is new`() {
        val appUser = AppUser(appUsername = "john", clientPassword = "pass123")

        `when`(appUserRepository.existsAppUserByAppUsername("john")).thenReturn(false)
        `when`(passwordEncoder.encode("pass123")).thenReturn("encodedPass")
        `when`(appUserRepository.save(any(AppUser::class.java))).thenReturn(appUser.copy(clientPassword = "encodedPass"))

        val response = appUserService.signUp(appUser)
        assertEquals("User created successfully", response.message)
    }

    @Test
    fun `resetPassword should fail if old password does not match`() {
        val appUser = AppUser(appUsername = "john", clientPassword = "encodedPass")

        `when`(appUserRepository.findAppUserByAppUsername("john")).thenReturn(appUser)
        `when`(passwordEncoder.matches("wrongOld", "encodedPass")).thenReturn(false)

        val request = ResetPasswordRequest("john", "wrongOld", "Newpass@1")
        val response = appUserService.resetPassword(request)
        assertEquals("Old password is incorrect.", response.message)
    }

    @Test
    fun `signIn should authenticate and return token`() {
        val appUser = AppUser(appUsername = "john", clientPassword = "encoded")

        `when`(appUserRepository.findAppUserByAppUsername("john")).thenReturn(appUser)
        `when`(jwtService.generateAccessToken(appUser)).thenReturn("token")

        val request = LoginRequest("john", "password")
        val response = appUserService.signIn(request)

        assertEquals("User login successfully", response.message)
        assertEquals("token", (response.metadata as LoginResponse).accessToken)
    }

    @Test
    fun `getUserById should return user with presigned url`() {
        val appUser = AppUser(id = 1L, appUsername = "john", profilePicture = "john.png")

        `when`(appUserRepository.findById(1L)).thenReturn(Optional.of(appUser))
        `when`(s3Service.generatePresignedUrl("john.png")).thenReturn("https://presigned.url/john.png")

        val response = appUserService.getUserById(1L)
        val profileUrl = response.data?.profilePicture
        assertEquals("https://presigned.url/john.png", profileUrl)
    }

    @Test
    fun `deleteUser should remove user from repository`() {
        val appUser = AppUser(id = 1L, appUsername = "john")

        `when`(appUserRepository.findById(1L)).thenReturn(Optional.of(appUser))

        val response = appUserService.deleteUser(1L)

        verify(appUserRepository).delete(appUser)
        assertEquals("User deleted successfully", response.message)
    }
}