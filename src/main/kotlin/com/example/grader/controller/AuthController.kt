package com.example.grader.controller

import com.example.grader.dto.*
import com.example.grader.entity.AppUser
import com.example.grader.service.AppUserService


import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val appUserService: AppUserService) {
    @PostMapping("/signUp")
    fun signUp(@RequestBody appUser: AppUser): ResponseEntity<ApiResponse<AppUserDto>> {
        val response: ApiResponse<AppUserDto> = appUserService.signUp(appUser)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/signIn")
    fun signIn(@RequestBody loginRequest: LoginRequest): ResponseEntity<ApiResponse<AppUserDto>?> {
        val response: ApiResponse<AppUserDto> = appUserService.signIn(loginRequest)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/resetPassword")
    fun resetPassword(@RequestBody resetPasswordRequest: ResetPasswordRequest,

    ): ResponseEntity<ApiResponse<AppUserDto>> {
        val response: ApiResponse<AppUserDto> = appUserService.resetPassword(resetPasswordRequest)
        return  ResponseEntity.ok(response)
    }
}