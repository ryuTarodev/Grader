package com.example.grader.service

import com.example.grader.dto.RequesttResponse.SubmissionRequest
import com.example.grader.dto.SubmissionDto
import com.example.grader.dto.SubmissionSendMessage
import com.example.grader.entity.Status
import com.example.grader.entity.Submission
import com.example.grader.error.*
import com.example.grader.repository.*
import com.example.grader.util.*
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubmissionService(
    private val problemRepository: ProblemRepository,
    private val appUserRepository: AppUserRepository,
    private val submissionRepository: SubmissionRepository,
    private val testCaseRepository: TestCaseRepository,
    private val submissionProducer: SubmissionProducer
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createSubmission(problemId: Long, appUserId: Long, submissionRequest: SubmissionRequest): SubmissionDto {
        // Validate input
        validateSubmissionRequest(submissionRequest)

        logger.info("Creating submission for problemId: $problemId, userId: $appUserId")

        // Validate entities exist
        val appUser = appUserRepository.findByIdOrNull(appUserId)
            ?: throw UserNotFoundException("User not found with ID: $appUserId")

        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("Problem not found with ID: $problemId")

        // Get test cases for the problem
        val testCases = testCaseRepository.findByProblemId(problemId)
        if (testCases.isNullOrEmpty()) {
            throw TestCaseNotFoundException("No test cases found for problem ID: $problemId")
        }

        // Create and save submission
        val submission = Submission(
            appUser = appUser,
            problem = problem,
            code = submissionRequest.code.trim(),
            language = submissionRequest.language.trim(),
            status = Status.PENDING // Set initial status
        )

        val savedSubmission = submissionRepository.save(submission)
        logger.info("Submission created successfully with ID: ${savedSubmission.id}")

        // Prepare message for RabbitMQ
        val submissionDto = savedSubmission.toSubmissionDTO()
        val message = SubmissionSendMessage(
            submissionDto,
            mapTestCaseListEntityToTestCaseListDTO(testCases)
        )

        // Send to RabbitMQ for processing
        try {
            logger.info("Sending submission ${savedSubmission.id} to RabbitMQ for processing")
            submissionProducer.sendSubmission(message)
        } catch (e: Exception) {
            logger.error("Failed to send submission ${savedSubmission.id} to RabbitMQ", e)
            // You might want to update submission status to ERROR here
            throw BadRequestException("Failed to queue submission for processing")
        }

        return submissionDto
    }

    @Transactional
    fun updateSubmissionFields(submissionId: Long, submissionRequest: SubmissionRequest): SubmissionDto {
        // Validate input
        validateSubmissionRequest(submissionRequest)

        logger.info("Updating submission fields for ID: $submissionId")

        val submission = submissionRepository.findByIdOrNull(submissionId)
            ?: throw SubmissionNotFoundException("Submission not found with ID: $submissionId")

        // Only allow updates if submission is not yet processed
        if (submission.status == Status.ACCEPTED || submission.status == Status.REJECTED) {
            throw BadRequestException("Cannot update submission that has already been processed")
        }

        // Update fields
        submission.code = submissionRequest.code.trim()
        submission.language = submissionRequest.language.trim()
        submission.status = Status.PENDING // Reset status since code changed

        val updatedSubmission = submissionRepository.save(submission)
        logger.info("Submission updated successfully with ID: $submissionId")

        return updatedSubmission.toSubmissionDTO()
    }

    @Transactional
    fun updateSubmissionResult(submissionId: Long, score: Float): SubmissionDto {
        logger.info("Updating submission result for ID: $submissionId with score: $score")

        // Validate score
        if (score < 0f || score > 100f) {
            throw BadRequestException("Score must be between 0 and 100")
        }

        val submission = submissionRepository.findByIdOrNull(submissionId)
            ?: throw SubmissionNotFoundException("Submission not found with ID: $submissionId")

        // Update submission result
        submission.score = score
        submission.status = determineStatus(score)

        val updatedSubmission = submissionRepository.save(submission)
        logger.info("Submission result updated successfully for ID: $submissionId, status: ${submission.status}")

        return updatedSubmission.toSubmissionDTO()
    }

    fun getSubmissionsByProblemAndUser(problemId: Long, appUserId: Long): List<SubmissionDto> {
        logger.info("Retrieving submissions for problemId: $problemId and userId: $appUserId")

        // Validate that problem and user exist
        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("Problem not found with ID: $problemId")

        val user = appUserRepository.findByIdOrNull(appUserId)
            ?: throw UserNotFoundException("User not found with ID: $appUserId")

        val submissions = submissionRepository.findAllByProblemIdAndAppUserId(problemId, appUserId)
        return mapSubmissionListEntityToSubmissionListDTO(submissions ?: emptyList())
    }

    fun getSubmissionById(submissionId: Long): SubmissionDto {
        logger.info("Retrieving submission with ID: $submissionId")

        val submission = submissionRepository.findByIdOrNull(submissionId)
            ?: throw SubmissionNotFoundException("Submission not found with ID: $submissionId")

        return submission.toSubmissionDTO()
    }

    fun getSubmissionsByUser(appUserId: Long): List<SubmissionDto> {
        logger.info("Retrieving all submissions for userId: $appUserId")

        // Validate that user exists
        val user = appUserRepository.findByIdOrNull(appUserId)
            ?: throw UserNotFoundException("User not found with ID: $appUserId")

        val submissions = submissionRepository.findAllByAppUserId(appUserId)
        return mapSubmissionListEntityToSubmissionListDTO(submissions ?: emptyList())
    }

    fun getSubmissionsByProblem(problemId: Long): List<SubmissionDto> {
        logger.info("Retrieving all submissions for problemId: $problemId")

        // Validate that problem exists
        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("Problem not found with ID: $problemId")

        val submissions = submissionRepository.findAllByProblemId(problemId)
        return mapSubmissionListEntityToSubmissionListDTO(submissions ?: emptyList())
    }

    @Transactional
    fun deleteAllSubmissionsByProblemAndUser(problemId: Long, appUserId: Long) {
        logger.info("Deleting all submissions for problemId: $problemId and userId: $appUserId")

        // Validate that problem and user exist
        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("Problem not found with ID: $problemId")

        val user = appUserRepository.findByIdOrNull(appUserId)
            ?: throw UserNotFoundException("User not found with ID: $appUserId")

        val deletedCount = submissionRepository.countByProblemIdAndAppUserId(problemId, appUserId)
        submissionRepository.deleteAllByProblemIdAndAppUserId(problemId, appUserId)

        logger.info("Deleted $deletedCount submissions for problemId: $problemId and userId: $appUserId")
    }

    @Transactional
    fun deleteSubmission(submissionId: Long) {
        logger.info("Deleting submission with ID: $submissionId")

        val submission = submissionRepository.findByIdOrNull(submissionId)
            ?: throw SubmissionNotFoundException("Submission not found with ID: $submissionId")

        submissionRepository.delete(submission)
        logger.info("Submission deleted successfully with ID: $submissionId")
    }

    // Private helper methods
    private fun validateSubmissionRequest(request: SubmissionRequest) {
        if (request.code.isBlank()) {
            throw BadRequestException("Submission code cannot be blank")
        }
        if (request.language.isBlank()) {
            throw BadRequestException("Programming language cannot be blank")
        }
        if (request.code.length > 10000) { // Reasonable limit
            throw BadRequestException("Submission code is too long (max 10000 characters)")
        }
    }

    private fun determineStatus(score: Float): Status {
        return when {
            score <= 0f -> Status.REJECTED
            score >= 100f -> Status.ACCEPTED
            else -> Status.PENDING // You might want to add this status or use a different logic
        }
    }
}