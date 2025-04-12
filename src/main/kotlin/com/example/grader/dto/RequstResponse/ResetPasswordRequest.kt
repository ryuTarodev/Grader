package com.example.grader.dto.RequstResponse

import jakarta.validation.constraints.NotBlank


data class ResetPasswordRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,
    @field:NotBlank(message = "Password is required")
    val oldPassword: String,
    @field:NotBlank(message = "NewPassword is required")
    val newPassword: String,
    )