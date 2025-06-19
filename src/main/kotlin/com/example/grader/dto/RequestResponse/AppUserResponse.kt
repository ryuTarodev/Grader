package com.example.grader.dto.RequestResponse

import com.example.grader.dto.AppUserDto
import com.example.grader.entity.Role

data class AppUserResponse(
    val appUser: AppUserDto,
    val token: String = "",
    )
