package com.example.grader.util

import com.ryutaro.grader.dto.ApiResponse

object ResponseUtil {

    fun <T> success(message: String, data: T, metadata: Any?): ApiResponse<T> {
        return ApiResponse("200", message, data, metadata)
    }

    fun <T> created(message: String, data: T, metadata: Any?): ApiResponse<T> {
        return ApiResponse("201", message, data, metadata)
    }

    fun <T> noContent(message: String): ApiResponse<T> {
        return ApiResponse("204", message, null as T?, null)
    }

    fun <T> badRequest(message: String, data: T): ApiResponse<T> {
        return ApiResponse("400", message, data, null)
    }

    fun <T> unauthorized(message: String, data: T): ApiResponse<T> {
        return ApiResponse("401", message, data, null)
    }

    fun <T> forbidden(message: String, data: T): ApiResponse<T> {
        return ApiResponse("403", message, data, null)
    }

    fun <T> notFound(message: String, data: T): ApiResponse<T> {
        return ApiResponse("404", message, data, null)
    }

    fun <T> conflict(message: String, data: T): ApiResponse<T> {
        return ApiResponse("409", message, data, null)
    }

    fun <T> internalServerError(message: String, data: T): ApiResponse<T> {
        return ApiResponse("500", message, data, null)
    }

    fun <T> notImplemented(message: String, data: T): ApiResponse<T> {
        return ApiResponse("501", message, data, null)
    }

    fun <T> serviceUnavailable(message: String, data: T): ApiResponse<T> {
        return ApiResponse("503", message, data, null)
    }
}