package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.RequstResponse.SubmissionRequest
import com.example.grader.dto.SubmissionDto
import com.example.grader.service.SubmissionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/submissions")
class SubmissionController(private val submissionService: SubmissionService) {

    @PostMapping("/problems/{problemId}/user/{appUserId}")
    fun sendSubmission(
        @PathVariable problemId: Long,
        @PathVariable appUserId: Long,
        @RequestBody submissionRequest: SubmissionRequest
    ): ResponseEntity<ApiResponse<SubmissionDto>> {
        val response = submissionService.createSubmission(
            problemId,
            appUserId,
            submissionRequest.code,
            submissionRequest.language
        )
        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PutMapping("/{submissionId}")
    fun updateSubmissionFields(
        @PathVariable submissionId: Long,
        @RequestBody request: SubmissionRequest
    ): ResponseEntity<ApiResponse<SubmissionDto>> {
        val response = submissionService.updateSubmissionFields(submissionId, request)
        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PutMapping("/{submissionId}/result")
    fun updateSubmissionResult(
        @PathVariable submissionId: Long,
        @RequestParam score: Float
    ): ResponseEntity<ApiResponse<SubmissionDto>> {
        val response = submissionService.updateSubmissionResult(submissionId, score)
        return ResponseEntity.status(response.statusCode).body(response)
    }

    @GetMapping("/problems/{problemId}/user/{appUserId}")
    fun getSubmissions(
        @PathVariable problemId: Long,
        @PathVariable appUserId: Long
    ): ApiResponse<List<SubmissionDto>> {
        return submissionService.getSubmissionByProblemIdAndAppUserId(problemId, appUserId)
    }

    @DeleteMapping("/problems/{problemId}/user/{appUserId}")
    fun clearAllSubmissions(
        @PathVariable problemId: Long,
        @PathVariable appUserId: Long
    ): ApiResponse<Unit> {
        return submissionService.deleteAllSubmissions(problemId, appUserId)
    }
}