package com.example.grader.dto.RequestResponse

import com.example.grader.entity.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class AppUserRequest(
    @field:NotNull @field:NotEmpty @field:NotBlank
    val username: String,
    @field:NotNull @field:NotEmpty @field:NotBlank
    val password: String,
    @field:NotNull @field:NotEmpty @field:NotBlank
    val role: Role,
    val profilePicture: String? = null,
)