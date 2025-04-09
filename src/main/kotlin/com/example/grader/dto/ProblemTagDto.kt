package com.example.grader.dto

import com.example.grader.entity.Problem
import com.example.grader.entity.Tag
import java.io.Serializable

/**
 * DTO for {@link com.example.grader.entity.ProblemTag}
 */
data class ProblemTagDto(
    val id: Long? = null,
    val problem: ProblemDto? = null,
    val tag: TagDto? = null
) : Serializable