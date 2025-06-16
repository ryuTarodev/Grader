package com.example.grader.dto.RequesttResponse

import com.example.grader.entity.Type
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class TestCaseRequest(
    val input: String,
    val output: String,
    @field:NotNull @field:NotEmpty @field:NotBlank
    val type: Type
)