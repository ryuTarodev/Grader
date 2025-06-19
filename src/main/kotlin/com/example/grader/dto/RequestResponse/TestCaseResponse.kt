package com.example.grader.dto.RequestResponse

import com.example.grader.entity.Type

class TestCaseResponse(
    val id: Long? = null,
    val problemId: Long? = null,
    val input: String = "",
    val output: String = "",
    val type: Type = Type.PRIVATE
) {

}