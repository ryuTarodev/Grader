package com.example.grader.dto.RequesttResponse

import com.example.grader.entity.Role

data class AppUserResponse(
    val id: Long = 0,
    val appUsername: String = "",
    val role: Role = Role.ROLE_USER,
    var profilePicture: String = ""
)
