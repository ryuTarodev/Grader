package com.example.grader.dto

import com.example.grader.entity.Submission
import com.example.grader.entity.TestCase

data class SubmissionMessage (
    val submissionDto: SubmissionDto,
    val testCasesDtoList: List<TestCaseDto>,
    )