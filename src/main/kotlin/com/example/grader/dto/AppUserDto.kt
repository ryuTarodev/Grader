package com.example.grader.dto

import com.example.grader.entity.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.io.Serializable

/**
 * DTO for {@link com.example.grader.entity.AppUser}
 */
data class AppUserDto(
    val id: Long = 0,
    val appUsername: String = "",
    var profilePicture: String = ""
) : Serializable