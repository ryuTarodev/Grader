package com.example.grader.service

import com.example.grader.dto.RequestResponse.*
import com.example.grader.entity.AppUser
import com.example.grader.entity.Role
import com.example.grader.error.*
import com.example.grader.repository.AppUserRepository
import com.example.grader.security.JwtService
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.multipart.MultipartFile
import java.util.*

class AppUserServiceTest {

    private lateinit var appUserRepository: AppUserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var clientSessionService: ClientSessionService
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var jwtService: JwtService
    private lateinit var s3Service: AwsS3Service
    private lateinit var appUserService: AppUserService

    private val dummyUser = AppUser(
        id = 1L,
        appUsername = "john",
        clientPassword = "encodedPass",
        role = Role.ROLE_USER,
        profilePicture = "pic.png"
    )

    @BeforeEach
    fun setup() {
        appUserRepository = mock(AppUserRepository::class.java)
        passwordEncoder = mock(PasswordEncoder::class.java)
        clientSessionService = mock(ClientSessionService::class.java)
        authenticationManager = mock(AuthenticationManager::class.java)
        jwtService = mock(JwtService::class.java)
        s3Service = mock(AwsS3Service::class.java)

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
    fun `signUp should create new user`() {
        val request = AppUserRequest("john", "password123!", Role.ROLE_USER, null)
        `when`(appUserRepository.existsAppUserByAppUsername("john")).thenReturn(false)
        `when`(passwordEncoder.encode("password123!")).thenReturn("encodedPassword")
        `when`(appUserRepository.save(any())).thenReturn(dummyUser)

        val result = appUserService.signUp(request)
        assertEquals("john", result.appUsername)
    }

    @Test
    fun `signUp should throw DuplicateException if username exists`() {
        `when`(appUserRepository.existsAppUserByAppUsername("john")).thenReturn(true)

        val ex = assertThrows<DuplicateException> {
            appUserService.signUp(AppUserRequest("john", "pass", Role.ROLE_USER, null))
        }
        assertTrue(ex.message!!.contains("already exists"))
    }

    @Test
    fun `resetPassword should change password`() {
        val request = ResetPasswordRequest("john", "oldPass", "Newpass@123")
        `when`(appUserRepository.findAppUserByAppUsername("john")).thenReturn(dummyUser)
        `when`(passwordEncoder.matches("oldPass", "encodedPass")).thenReturn(true)
        `when`(passwordEncoder.matches("Newpass@123", "encodedPass")).thenReturn(false)
        `when`(passwordEncoder.encode("Newpass@123")).thenReturn("newEncodedPass")
        `when`(appUserRepository.save(any())).thenReturn(dummyUser)

        val result = appUserService.resetPassword(request)
        assertEquals("john", result.appUsername)
    }

    @Test
    fun `resetPassword should fail with empty fields`() {
        val request = ResetPasswordRequest("john", "", "new")

        val ex = assertThrows<BadRequestException> {
            appUserService.resetPassword(request)
        }
        assertTrue(ex.message!!.contains("must not be empty"))
    }

    @Test
    fun `resetPassword should fail with invalid new password`() {
        val request = ResetPasswordRequest("john", "old", "short")
        `when`(appUserRepository.findAppUserByAppUsername("john")).thenReturn(dummyUser)
        `when`(passwordEncoder.matches("old", "encodedPass")).thenReturn(true)
        `when`(passwordEncoder.matches("short", "encodedPass")).thenReturn(false)

        val ex = assertThrows<BadRequestException> {
            appUserService.resetPassword(request)
        }
        assertTrue(ex.message!!.contains("complexity"))
    }

    @Test
    fun `signIn should return tokens and user dto`() {
        val request = LoginRequest("john", "password")
        `when`(appUserRepository.findAppUserByAppUsername("john")).thenReturn(dummyUser)
        `when`(jwtService.generateAccessToken(dummyUser)).thenReturn("token")

        val result = appUserService.signIn(request)

        assertEquals("john", result.first.appUsername)
        assertEquals("token", result.second.accessToken)
    }

    @Test
    fun `getAppUsers should return all users`() {
        `when`(appUserRepository.findAll()).thenReturn(listOf(dummyUser))
        val result = appUserService.getAppUsers()
        assertEquals(1, result.size)
    }

    @Test
    fun `getUserById should return user dto with presigned URL`() {
        `when`(appUserRepository.findById(1L)).thenReturn(Optional.of(dummyUser))
        `when`(s3Service.generatePresignedUrl("pic.png")).thenReturn("url.com/pic.png")

        val result = appUserService.getUserById(1L)
        assertEquals("url.com/pic.png", result.profilePicture)
    }

    @Test
    fun `uploadUserProfile should update profile picture`() {
        val file = mock(MultipartFile::class.java)
        `when`(s3Service.savePngToS3(file)).thenReturn("newPic.png")
        `when`(appUserRepository.findById(1L)).thenReturn(Optional.of(dummyUser))
        `when`(appUserRepository.save(any())).thenReturn(dummyUser)

        val result = appUserService.uploadUserProfile(1L, file)
        assertEquals("john", result.appUsername)
    }

    @Test
    fun `updateUser should change username and role`() {
        val request = AppUserRequest("newUser", "pass", Role.ROLE_ADMIN, "new.png")
        `when`(appUserRepository.findById(1L)).thenReturn(Optional.of(dummyUser))
        `when`(appUserRepository.existsAppUserByAppUsername("newUser")).thenReturn(false)
        `when`(appUserRepository.save(any())).thenReturn(dummyUser)

        val result = appUserService.updateUser(1L, request)
        assertEquals("newUser", result.appUsername)
    }

    @Test
    fun `deleteUser should delete user if found`() {
        `when`(appUserRepository.findById(1L)).thenReturn(Optional.of(dummyUser))
        doNothing().`when`(appUserRepository).delete(dummyUser)

        assertDoesNotThrow { appUserService.deleteUser(1L) }
    }

    @Test
    fun `deleteUser should throw exception if user not found`() {
        `when`(appUserRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> { appUserService.deleteUser(1L) }
    }
}