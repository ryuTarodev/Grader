package com.example.grader.dto.RequestResponse

import com.example.grader.dto.AppUserDto

data class InternalAppUserResponse(
    val appUser: AppUserDto,
    val accessToken: String,
    val refreshToken: String
)