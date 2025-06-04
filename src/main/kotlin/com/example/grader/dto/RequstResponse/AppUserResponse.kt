package com.example.grader.dto.RequstResponse

import com.example.grader.entity.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class AppUserResponse(
    val id: Long = 0,
    val appUsername: String = "",
    val role: Role = Role.ROLE_USER,
    var profilePicture: String = ""
)
