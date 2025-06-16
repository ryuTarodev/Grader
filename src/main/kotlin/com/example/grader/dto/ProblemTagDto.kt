package com.example.grader.dto

import java.io.Serializable

/**
 * DTO for {@link com.example.grader.entity.ProblemTag}
 */
data class ProblemTagDto(
    val id: Long? = null,
    val problemId: Long? = null,
    val tag: Long? = null,
) : Serializable