package com.example.grader.dto

import com.example.grader.entity.Status
import java.io.Serializable
import java.time.Instant

/**
 * DTO for {@link com.example.grader.entity.Submission}
 */
data class SubmissionDto(
    val id: Long? = null,
    val appUserId: Long? = null,
    val problemId: Long? = null,
    val code: String? = null,
    val score: Float? = 0f,
    val language: String? = null,
    val status: Status = Status.PENDING,
    val submittedAt: Instant = Instant.now()
) : Serializable