package com.example.grader.dto

import com.ryutaro.grader.entity.Role
import jakarta.validation.constraints.NotBlank
data class LoginResponse(

    val expirationTime: String? = "1 Days",
    val accessToken: String,

)