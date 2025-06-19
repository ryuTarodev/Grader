package com.example.grader.service

import com.example.grader.dto.RequestResponse.SubmissionRequest
import com.example.grader.dto.SubmissionDto
import com.example.grader.dto.SubmissionSendMessage
import com.example.grader.entity.AppUser
import com.example.grader.entity.Status
import com.example.grader.entity.Submission
import com.example.grader.error.*
import com.example.grader.repository.*
import com.example.grader.util.*
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
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

    fun retrieveAuthentication(): Authentication? {
        return SecurityContextHolder.getContext().authentication
    }

    private fun getAuthenticatedUser(): AppUser {
        val authentication = retrieveAuthentication()
            ?: throw UserNotFoundException("Authenticated user not found")

        val username = when (val principal = authentication.principal) {
            is UserDetails -> principal.username
            else -> principal.toString()
        }

        return appUserRepository.findAppUserByAppUsername(username)
            ?: throw UserNotFoundException("User not found with username: $username")
    }

    @Transactional
    fun createSubmission(problemId: Long, submissionRequest: SubmissionRequest): SubmissionDto {
        validateSubmissionRequest(submissionRequest)

        val appUser = getAuthenticatedUser()
        logger.info("Creating submission for problemId: $problemId, user: $appUser")

        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("Problem not found with ID: $problemId")

        val testCases = testCaseRepository.findByProblemId(problemId)
        if (testCases.isEmpty()) {
            throw TestCaseNotFoundException("No test cases found for problem ID: $problemId")
        }

        val submission = Submission(
            appUser = appUser,
            problem = problem,
            code = submissionRequest.code.trim(),
            language = submissionRequest.language.trim(),
            status = Status.PENDING
        )

        val savedSubmission = submissionRepository.save(submission)
        logger.info("Submission created successfully with ID: ${savedSubmission.id}")

        val submissionDto = savedSubmission.toSubmissionDTO()
        val message = SubmissionSendMessage(
            submissionDto = submissionDto,
            testCasesDtoList = mapTestCaseListEntityToTestCaseListDTO(testCases)
        )

        try {
            logger.info("Sending submission ${savedSubmission.id} to RabbitMQ for processing")
            submissionProducer.sendSubmission(message)
        } catch (e: Exception) {
            logger.error("Failed to send submission ${savedSubmission.id} to RabbitMQ", e)
            throw SubmissionSendException("Failed to send submission ${savedSubmission.id} to RabbitMQ")
        }

        return submissionDto
    }

    @Transactional
    fun updateSubmissionFields(submissionId: Long, submissionRequest: SubmissionRequest): SubmissionDto {
        validateSubmissionRequest(submissionRequest)

        logger.info("Updating submission fields for ID: $submissionId")

        val submission = submissionRepository.findByIdOrNull(submissionId)
            ?: throw SubmissionNotFoundException("Submission not found with ID: $submissionId")

        if (submission.status == Status.ACCEPTED || submission.status == Status.REJECTED) {
            throw BadRequestException("Cannot update submission that has already been processed")
        }

        submission.code = submissionRequest.code.trim()
        submission.language = submissionRequest.language.trim()
        submission.status = Status.PENDING

        val updatedSubmission = submissionRepository.save(submission)
        logger.info("Submission updated successfully with ID: $submissionId")

        return updatedSubmission.toSubmissionDTO()
    }

    @Transactional
    fun updateSubmissionResult(submissionId: Long, score: Float): SubmissionDto {
        logger.info("Updating submission result for ID: $submissionId with score: $score")

        if (score < 0f || score > 100f) {
            throw BadRequestException("Score must be between 0 and 100")
        }

        val submission = submissionRepository.findByIdOrNull(submissionId)
            ?: throw SubmissionNotFoundException("Submission not found with ID: $submissionId")

        submission.score = score
        submission.status = determineStatus(score)

        val updatedSubmission = submissionRepository.save(submission)
        logger.info("Submission result updated successfully for ID: $submissionId, status: ${submission.status}")

        return updatedSubmission.toSubmissionDTO()
    }

    fun getSubmissionsByProblemAndUser(problemId: Long): List<SubmissionDto> {
        logger.info("Retrieving submissions for problemId: $problemId")

        val appUser = getAuthenticatedUser()

        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("Problem not found with ID: $problemId")

        val submissions = submissionRepository.findAllByProblemIdAndAppUserId(problemId, appUser.id)
        return mapSubmissionListEntityToSubmissionListDTO(submissions ?: emptyList())
    }

    fun getSubmissionById(submissionId: Long): SubmissionDto {
        logger.info("Retrieving submission with ID: $submissionId")

        val submission = submissionRepository.findByIdOrNull(submissionId)
            ?: throw SubmissionNotFoundException("Submission not found with ID: $submissionId")

        return submission.toSubmissionDTO()
    }


    @Transactional
    fun deleteAllSubmissionsByProblemAndUser(problemId: Long) {
        logger.info("Deleting all submissions for problemId: $problemId and the authenticated user")

        val appUser = getAuthenticatedUser()

        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("Problem not found with ID: $problemId")

        val deletedCount = submissionRepository.countByProblemIdAndAppUserId(problemId, appUser.id)
        submissionRepository.deleteAllByProblemIdAndAppUserId(problemId, appUser.id)

        logger.info("Deleted $deletedCount submissions for problemId: $problemId and userId: ${appUser.id}")
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
        if (request.code.length > 10000) {
            throw BadRequestException("Submission code is too long (max 10000 characters)")
        }
    }

    private fun determineStatus(score: Float): Status {
        return when {
            score < 0f || score > 100f -> Status.REJECTED
            score in 0.0..100.0 -> Status.ACCEPTED
            else -> Status.PENDING
        }
    }
}