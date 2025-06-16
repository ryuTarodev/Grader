package com.example.grader.dto.RequesttResponse

import com.example.grader.entity.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class AppUserRequest(
    @field:NotNull @field:NotEmpty @field:NotBlank
    val appUsername: String,

    @field:NotNull @field:NotEmpty @field:NotBlank
    val clientPassword: String,
    @field:NotNull @field:NotEmpty @field:NotBlank
    val role: Role,
    val profilePicture: String? = null,
)