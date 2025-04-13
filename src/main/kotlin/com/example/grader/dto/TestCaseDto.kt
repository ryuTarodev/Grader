package com.example.grader.dto

import com.example.grader.entity.Type
import java.io.Serializable

/**
 * DTO for {@link com.example.grader.entity.TestCase}
 */
data class TestCaseDto(
    val id: Long? = null,
    val problemId: Long? = null,
    val input: String = "",
    val output: String = "",
    val type: Type = Type.PRIVATE
) : Serializable