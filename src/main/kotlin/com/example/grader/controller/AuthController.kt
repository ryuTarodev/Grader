package com.example.grader.controller

import com.example.grader.dto.*
import com.example.grader.dto.RequesttResponse.AppUserRequest
import com.example.grader.dto.RequesttResponse.LoginRequest
import com.example.grader.dto.RequesttResponse.LoginResponse
import com.example.grader.dto.RequesttResponse.ResetPasswordRequest
import com.example.grader.entity.AppUser
import com.example.grader.service.AppUserService
import com.example.grader.util.ResponseUtil
import org.springframework.http.HttpStatus


import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val appUserService: AppUserService) {
    @PostMapping("/signUp")
    fun signUp(@RequestBody appUserRequest: AppUserRequest): ResponseEntity<ApiResponse<AppUserDto>> {
        val appUser = appUserService.signUp(appUserRequest)
        val response = ResponseUtil.success("AppUser signed up successfully", appUser, null)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/signIn")
    fun signIn(@RequestBody loginRequest: LoginRequest): ResponseEntity<ApiResponse<AppUserDto>> {
        val pair = appUserService.signIn(loginRequest)
        val response = ResponseUtil.success("AppUser signed in", pair.first, pair.second)
        return ResponseEntity.ok(response)
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