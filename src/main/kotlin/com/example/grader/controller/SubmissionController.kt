package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.RequstResponse.SubmissionRequest
import com.example.grader.dto.SubmissionDto
import com.example.grader.service.SubmissionService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/problems/{problemId}/user/{userId}/submissions")
class SubmissionController(private val submissionService: SubmissionService) {


    @PostMapping("")
    fun createSubmission(
        @PathVariable problemId: Long,
        @PathVariable userId: Long,
        @RequestBody submissionRequest: SubmissionRequest
    ): ApiResponse<SubmissionDto> {
        submissionRequest.problemId = problemId
        submissionRequest.appUserId = userId
        return submissionService.createSubmission(submissionRequest)
    }

}