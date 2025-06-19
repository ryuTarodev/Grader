package com.example.grader.dto.RequestResponse

data class LoginResponse(
    val expirationTime: String? = "1 Days",
    val accessToken: String,
)