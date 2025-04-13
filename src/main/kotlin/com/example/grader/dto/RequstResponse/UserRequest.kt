package com.example.grader.dto.RequstResponse

import com.example.grader.entity.Role

data class UserRequest(
    val appUsername: String,
    val role: Role,
    val profilePicture: String
)