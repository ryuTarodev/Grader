package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.RequesttResponse.SubmissionRequest
import com.example.grader.dto.SubmissionDto
import com.example.grader.service.SubmissionService
import com.example.grader.util.ResponseUtil
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/submissions")
class SubmissionController(
    private val submissionService: SubmissionService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Create a new submission
    @PostMapping("/problems/{problemId}/users/{userId}")
    fun createSubmission(
        @PathVariable problemId: Long,
        @PathVariable userId: Long,
        @Valid @RequestBody submissionRequest: SubmissionRequest
    ): ResponseEntity<ApiResponse<SubmissionDto>> {
        logger.info("Received request to create submission for problemId: $problemId, userId: $userId")

        val submission = submissionService.createSubmission(problemId, userId, submissionRequest)
        val response = ResponseUtil.created("Submission created successfully", submission, null)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // Update submission fields (code, language)
    @PutMapping("/{submissionId}")
    fun updateSubmissionFields(
        @PathVariable submissionId: Long,
        @Valid @RequestBody submissionRequest: SubmissionRequest
    ): ResponseEntity<ApiResponse<SubmissionDto>> {
        logger.info("Received request to update submission fields for ID: $submissionId")

        val submission = submissionService.updateSubmissionFields(submissionId, submissionRequest)
        val response = ResponseUtil.success("Submission updated successfully", submission, null)
        return ResponseEntity.ok(response)
    }

    // Update submission result (score and status) - typically called by grading service
    @PatchMapping("/{submissionId}/result")
    fun updateSubmissionResult(
        @PathVariable submissionId: Long,
        @RequestParam score: Float
    ): ResponseEntity<ApiResponse<SubmissionDto>> {
        logger.info("Received request to update submission result for ID: $submissionId with score: $score")

        val submission = submissionService.updateSubmissionResult(submissionId, score)
        val response = ResponseUtil.success("Submission updated successfully", submission, null)
        return ResponseEntity.ok(response)
    }

    // Get a specific submission by ID
    @GetMapping("/{submissionId}")
    fun getSubmissionById(@PathVariable submissionId: Long): ResponseEntity<ApiResponse<SubmissionDto>> {
        logger.info("Received request to get submission with ID: $submissionId")

        val submission = submissionService.getSubmissionById(submissionId)
        val response = ResponseUtil.success("Submission returned successfully", submission, null)
        return ResponseEntity.ok(response)
    }

    // Get all submissions for a specific problem and user
    @GetMapping("/problems/{problemId}/users/{userId}")
    fun getSubmissionsByProblemAndUser(
        @PathVariable problemId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<ApiResponse<List<SubmissionDto>>> {
        logger.info("Received request to get submissions for problemId: $problemId and userId: $userId")

        val submissions = submissionService.getSubmissionsByProblemAndUser(problemId, userId)
        val response = ResponseUtil.success("Submissions returned successfully", submissions, null)
        return ResponseEntity.ok(response)
    }

    // Get all submissions by a specific user
    @GetMapping("/users/{userId}")
    fun getSubmissionsByUser(@PathVariable userId: Long): ResponseEntity<ApiResponse<List<SubmissionDto>>> {
        logger.info("Received request to get all submissions for userId: $userId")

        val submissions = submissionService.getSubmissionsByUser(userId)
        val response = ResponseUtil.success("Submissions returned successfully", submissions, null)
        return ResponseEntity.ok(response)
    }

    // Get all submissions for a specific problem
    @GetMapping("/problems/{problemId}")
    fun getSubmissionsByProblem(@PathVariable problemId: Long): ResponseEntity<ApiResponse<List<SubmissionDto>>> {
        logger.info("Received request to get all submissions for problemId: $problemId")

        val submissions = submissionService.getSubmissionsByProblem(problemId)
        val response = ResponseUtil.success("Submissions returned successfully", submissions, null)
        return ResponseEntity.ok(response)
    }

    // Delete a specific submission
    @DeleteMapping("/{submissionId}")
    fun deleteSubmission(@PathVariable submissionId: Long): ResponseEntity<ApiResponse<Unit>> {
        logger.info("Received request to delete submission with ID: $submissionId")

        submissionService.deleteSubmission(submissionId)
        val response = ResponseUtil.success("Submission deleted successfully", Unit,null)
        return ResponseEntity.ok(response)
    }

    // Delete all submissions for a specific problem and user
    @DeleteMapping("/problems/{problemId}/users/{userId}")
    fun deleteAllSubmissionsByProblemAndUser(
        @PathVariable problemId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        logger.info("Received request to delete all submissions for problemId: $problemId and userId: $userId")

        submissionService.deleteAllSubmissionsByProblemAndUser(problemId, userId)
        val response = ResponseUtil.success("Submission deleted successfully", Unit, null)
        return ResponseEntity.ok(response)
    }
}