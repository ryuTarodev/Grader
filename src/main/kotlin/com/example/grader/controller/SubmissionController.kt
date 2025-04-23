package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.RequstResponse.SubmissionRequest
import com.example.grader.dto.SubmissionDto
import com.example.grader.service.SubmissionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/problems/{problemId}/user/{appUserId}/submissions")
class SubmissionController(private val submissionService: SubmissionService) {


    @PostMapping("")
    fun sendSubmission(
        @PathVariable problemId: Long,
        @PathVariable appUserId: Long,
        @RequestBody submissionRequest: SubmissionRequest
    ): ResponseEntity<ApiResponse<SubmissionDto>> {
        val response = submissionService.createSubmission(problemId, appUserId, submissionRequest.code)
        return ResponseEntity.status(response.statusCode).body(response)
    }

    @GetMapping("")
    fun getSubmissions(
        @PathVariable problemId: Long,
        @PathVariable appUserId: Long,
    ): ApiResponse<List<SubmissionDto>> {
        return submissionService.getSubmissionByProblemIdAndAppUserId(problemId, appUserId)
    }

    @DeleteMapping("")
    fun deleteAllSubmissions(
        @PathVariable problemId: Long,
        @PathVariable appUserId: Long): ApiResponse<Unit> {
        return submissionService.deleteAllSubmissions(problemId, appUserId)
    }


}