package com.example.grader.dto.RequesttResponse

import com.example.grader.entity.Difficulty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile

class ProblemResponse (
    val id: Long? = null,
    @field:NotNull @field:NotEmpty @field:NotBlank
    val title: String,
    @field:NotNull @field:NotEmpty @field:NotBlank
    val difficulty: Difficulty,
    @field:NotNull @field:NotEmpty @field:NotBlank
    val pdf: MultipartFile
)