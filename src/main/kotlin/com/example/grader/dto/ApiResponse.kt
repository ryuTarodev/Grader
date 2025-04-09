package com.example.grader.dto

data class ApiResponse<T>(
    val statusCode: String,
    val message: String,
    val data: T?,
    val metadata: Any?
)