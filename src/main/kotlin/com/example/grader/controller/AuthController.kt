package com.example.grader.controller

import com.example.grader.dto.*
import com.example.grader.dto.RequestResponse.AppUserRequest
import com.example.grader.dto.RequestResponse.AppUserResponse
import com.example.grader.dto.RequestResponse.LoginRequest
import com.example.grader.dto.RequestResponse.ResetPasswordRequest
import com.example.grader.error.BadRequestException
import com.example.grader.service.AppUserService
import com.example.grader.util.ResponseUtil
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie


import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration


@RestController
@RequestMapping("/api/auth")
class AuthController(private val appUserService: AppUserService) {
    @PostMapping("/signUp")
    fun signUp(
        @RequestBody appUserRequest: AppUserRequest,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<AppUserResponse>> {
        val internalResponse = appUserService.signUp(appUserRequest)

        val cookie = createRefreshTokenCookie(internalResponse.refreshToken)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString())

        val publicResponse = AppUserResponse(
            appUser = internalResponse.appUser,
            accessToken = internalResponse.accessToken
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .headers(headers)
            .body(ResponseUtil.success("AppUser signed up and auto-logged in", publicResponse, "1 days"))
    }

    @PostMapping("/signIn")
    fun signIn(
        @RequestBody loginRequest: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<AppUserResponse>> {
        val internalResponse = appUserService.signIn(loginRequest)

        val cookie = createRefreshTokenCookie(internalResponse.refreshToken)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString())

        val publicResponse = AppUserResponse(
            appUser = internalResponse.appUser,
            accessToken = internalResponse.accessToken
        )

        return ResponseEntity.ok()
            .headers(headers)
            .body(ResponseUtil.success("AppUser signed in", publicResponse, "1 days"))
    }

    private fun createRefreshTokenCookie(refreshToken: String): ResponseCookie {
        return ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true) // Change to true in production!
            .path("/")
            .sameSite("Strict") // Or Lax if you support cross-origin
            .maxAge(Duration.ofDays(7))
            .build()
    }

    @PostMapping("/refresh")
    fun refreshTokenCookie(
        @CookieValue(value = "refreshToken", required = false) refreshToken: String?
    ): ResponseEntity<ApiResponse<AppUserResponse>> {
        if (refreshToken.isNullOrBlank()) {
            throw BadRequestException("Refresh token is missing")
        }

        val internalResponse = appUserService.refreshToken(refreshToken)

        val cookie = createRefreshTokenCookie(internalResponse.refreshToken)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString())

        val publicResponse = AppUserResponse(
            appUser = internalResponse.appUser,
            accessToken = internalResponse.accessToken
        )

        return ResponseEntity.ok()
            .headers(headers)
            .body(ResponseUtil.success("Token refreshed", publicResponse, "1 days"))
    }

    @PostMapping("/resetPassword")
    fun resetPassword(
        @RequestBody resetPasswordRequest: ResetPasswordRequest
    ): ResponseEntity<ApiResponse<AppUserDto>> {
        val appUser = appUserService.resetPassword(resetPasswordRequest)
        val response = ResponseUtil.success("AppUser reset password successfully", appUser, null)
        return ResponseEntity.ok(response)
    }
}