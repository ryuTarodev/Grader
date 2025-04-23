package com.example.grader.dto

data class SubmissionSendMessage (
    val submissionDto: SubmissionDto,
    val testCasesDtoList: List<TestCaseDto>,
    )