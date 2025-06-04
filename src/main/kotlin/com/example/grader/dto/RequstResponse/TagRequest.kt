package com.example.grader.dto.RequstResponse

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class TagRequest(
    @field:NotNull @field:NotEmpty @field:NotBlank
    val name: String,
    )