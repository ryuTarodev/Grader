package com.example.grader.dto

import java.io.Serializable

/**
 * DTO for {@link com.example.grader.entity.Tag}
 */
data class TagDto(
    val id: Long? = null,
    val name: String = ""
) : Serializable