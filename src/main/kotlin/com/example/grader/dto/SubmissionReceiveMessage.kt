package com.example.grader.dto

data class SubmissionReceiveMessage (
    val submissionId: Long,
    val correctTestCases: Long,
)