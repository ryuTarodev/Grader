package com.example.grader.dto

import com.ryutaro.grader.entity.Difficulty

import java.io.Serializable

/**
 * DTO for {@link com.example.grader.entity.Problem}
 */
data class ProblemDto(
    val id: Long? = null,
    val title: String = "",
    val difficulty : Difficulty = Difficulty.EASY,
    val pdf: String = ""
) : Serializable