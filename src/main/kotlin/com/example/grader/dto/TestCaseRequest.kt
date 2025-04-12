package com.example.grader.dto

import com.example.grader.entity.Type

data class TestCaseRequest(
    val problemId: Long,
    val input: String,
    val output: String,
    val type: Type
)