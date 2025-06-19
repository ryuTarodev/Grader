package com.example.grader.dto.RequestResponse

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull


data class ResetPasswordRequest(
    @field:NotBlank(message = "Username is required") @field:NotNull @field:NotEmpty
    val username: String,
    @field:NotBlank(message = "Password is required") @field:NotNull @field:NotEmpty
    val oldPassword: String,
    @field:NotBlank(message = "NewPassword is required") @field:NotNull @field:NotEmpty
    val newPassword: String,
    )