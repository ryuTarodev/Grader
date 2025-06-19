package com.example.grader.error

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.validation.FieldError

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    private fun buildResponseEntity(status: HttpStatus, message: String?, details: String? = null): ResponseEntity<ApiError> {
        val error = ApiError(
            message = message ?: "An error occurred",
            status = status,
            details = details
        )
        return ResponseEntity(error, status)
    }

    // Not Found Exceptions (404)
    @ExceptionHandler(
        UserNotFoundException::class,
        ProblemNotFoundException::class,
        TestCaseNotFoundException::class,
        SubmissionNotFoundException::class,
        TagNotFoundException::class,
        ProblemTagNotFoundException::class
    )
    fun handleNotFoundException(exception: RuntimeException): ResponseEntity<ApiError> {
        logger.warn("Resource not found: ${exception.message}")
        return buildResponseEntity(HttpStatus.NOT_FOUND, exception.message)
    }

    // Bad Request Exceptions (400)
    @ExceptionHandler(
        BadRequestException::class,
        AwsS3Exception::class,
        IllegalArgumentException::class
    )
    fun handleBadRequestException(exception: RuntimeException): ResponseEntity<ApiError> {
        logger.warn("Bad request: ${exception.message}")
        return buildResponseEntity(HttpStatus.BAD_REQUEST, exception.message)
    }

    // Unauthorized Exceptions (401)
    @ExceptionHandler(
        AccountVerificationException::class,
        TokenExpiredException::class,
        JwtAuthenticationException::class,
        BadCredentialsException::class,
        UnauthorizedException::class
    )
    fun handleUnauthorizedException(exception: RuntimeException): ResponseEntity<ApiError> {
        logger.warn("Unauthorized access: ${exception.message}")
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, exception.message)
    }

    // Conflict Exceptions (409)
    @ExceptionHandler(
        SignUpException::class,
        UsernameAlreadyExistsException::class,
        PasswordMismatchException::class,
        UsernamePasswordMismatchException::class,
        DuplicateException::class
    )
    fun handleConflictException(exception: RuntimeException): ResponseEntity<ApiError> {
        logger.warn("Conflict error: ${exception.message}")
        return buildResponseEntity(HttpStatus.CONFLICT, exception.message)
    }

    // Internal Server Error (500)
    @ExceptionHandler(
        JwtKeyException::class,
        SubmissionSendException::class
    )
    fun handleInternalServerException(exception: RuntimeException): ResponseEntity<ApiError> {
        logger.error("Internal server error: ${exception.message}", exception)
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error occurred")
    }

    // Validation Errors (400)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        logger.warn("Validation error: ${ex.message}")

        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { error: FieldError ->
            "${error.field}: ${error.defaultMessage}"
        }

        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Validation failed", errors)
    }

    // Generic Exception Handler (500)
    @ExceptionHandler(Exception::class)
    fun handleGenericException(exception: Exception): ResponseEntity<ApiError> {
        logger.error("Unexpected error occurred", exception)
        return buildResponseEntity(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred",
            if (logger.isDebugEnabled) exception.message else null
        )
    }
}

// Updated ApiError class
data class ApiError(
    val message: String,
    val status: HttpStatus,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

// Base Exception Classes
abstract class AppException(message: String) : RuntimeException(message)

abstract class AppNotFoundException(message: String) : AppException(message)
abstract class AppBadRequestException(message: String) : AppException(message)
abstract class AppUnauthorizedException(message: String) : AppException(message)
abstract class AppConflictException(message: String) : AppException(message)
abstract class AppInternalException(message: String) : AppException(message)

// Specific Exception Classes
// Not Found Exceptions
class UserNotFoundException(message: String) : AppNotFoundException(message)
class ProblemNotFoundException(message: String) : AppNotFoundException(message)
class TestCaseNotFoundException(message: String) : AppNotFoundException(message)
class SubmissionNotFoundException(message: String) : AppNotFoundException(message)
class TagNotFoundException(message: String) : AppNotFoundException(message)
class ProblemTagNotFoundException(message: String) : AppNotFoundException(message)

// Bad Request Exceptions
class BadRequestException(message: String) : AppBadRequestException(message)
class AwsS3Exception(message: String) : AppBadRequestException(message)

// Unauthorized Exceptions
class AccountVerificationException(message: String) : AppUnauthorizedException(message)
class TokenExpiredException(message: String) : AppUnauthorizedException(message)
class JwtAuthenticationException(message: String) : AppUnauthorizedException(message)
class UnauthorizedException(message: String) : AppUnauthorizedException(message)

// Conflict Exceptions
class SignUpException(message: String) : AppConflictException(message)
class UsernameAlreadyExistsException(message: String) : AppConflictException(message)
class PasswordMismatchException(message: String) : AppConflictException(message)
class UsernamePasswordMismatchException(message: String) : AppConflictException(message)
class DuplicateException(message: String) : AppConflictException(message)

// Internal Server Error
class JwtKeyException(message: String) : IllegalStateException(message)

// SubmissionSendException
class SubmissionSendException(message: String) : AppInternalException(message)