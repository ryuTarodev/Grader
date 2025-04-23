package com.example.grader.dto.RequstResponse

import com.example.grader.entity.Type

data class TestCaseRequest(
    val input: String,
    val output: String,
    val type: Type
)