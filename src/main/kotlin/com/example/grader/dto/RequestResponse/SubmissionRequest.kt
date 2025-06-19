package com.example.grader.dto.RequestResponse

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class SubmissionRequest (
    @field:NotNull @field:NotEmpty @field:NotBlank
    val code : String,
    @field:NotNull @field:NotEmpty @field:NotBlank
    val language: String,
)