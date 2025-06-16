package com.example.grader.dto.RequesttResponse

import com.example.grader.entity.Status
import java.time.Instant

class SubmissionResponse(
    val id: Long? = null,
    val appUserId: Long? = null,
    val problemId: Long? = null,
    val code: String? = null,
    val score: Float? = 0f,
    val language: String? = null,
    val status: Status = Status.PENDING,
    val submittedAt: Instant = Instant.now()
) {
}