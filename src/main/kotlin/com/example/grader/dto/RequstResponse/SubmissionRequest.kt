package com.example.grader.dto.RequstResponse

data class SubmissionRequest (
    var appUserId: Long,
    var problemId: Long,
    val code : String,
)