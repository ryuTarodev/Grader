package com.example.grader.dto

import com.ryutaro.grader.entity.Status
import java.io.Serializable
import java.time.Instant

/**
 * DTO for {@link com.example.grader.entity.Submission}
 */
data class SubmissionDto(
    val id: Long? = null,
    val appUser: AppUserDto? = null,
    val problem: ProblemDto? = null,
    val code: String? = null,
    val score: Float? = 0f,
    val status: Status = Status.PENDING,
    val submittedAt: Instant = Instant.now()
) : Serializable