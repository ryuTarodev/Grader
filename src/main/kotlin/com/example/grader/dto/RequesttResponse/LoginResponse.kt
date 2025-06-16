package com.example.grader.dto.RequesttResponse

data class LoginResponse(
    val expirationTime: String? = "1 Days",
    val accessToken: String,
)