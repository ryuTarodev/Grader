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
        logger.info("Start: createSubmission | problemId=$problemId, userId=${appUser.id}")

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
        logger.info("Submission saved | submissionId=${savedSubmission.id}")

        val submissionDto = savedSubmission.toSubmissionDTO()
        val message = SubmissionSendMessage(
            submissionDto = submissionDto,
            testCasesDtoList = mapTestCaseListEntityToTestCaseListDTO(testCases)
        )

        try {
            logger.info("Sending submission to queue | submissionId=${savedSubmission.id}")
            submissionProducer.sendSubmission(message)
        } catch (e: Exception) {
            logger.error("Failed to send submission to queue | submissionId=${savedSubmission.id}", e)
            throw SubmissionSendException("Failed to send submission ${savedSubmission.id} to RabbitMQ")
        }

        logger.info("End: createSubmission | submissionId=${savedSubmission.id}")
        return submissionDto
    }

    @Transactional
    fun updateSubmissionFields(submissionId: Long, submissionRequest: SubmissionRequest): SubmissionDto {
        validateSubmissionRequest(submissionRequest)

        logger.info("Start: updateSubmissionFields | submissionId=$submissionId")

        val submission = submissionRepository.findByIdOrNull(submissionId)
            ?: throw SubmissionNotFoundException("Submission not found with ID: $submissionId")

        if (submission.status == Status.ACCEPTED || submission.status == Status.REJECTED) {
            throw BadRequestException("Cannot update submission that has already been processed")
        }

        submission.code = submissionRequest.code.trim()
        submission.language = submissionRequest.language.trim()
        submission.status = Status.PENDING

        val updatedSubmission = submissionRepository.save(submission)
        logger.info("Submission updated | submissionId=${updatedSubmission.id}")

        return updatedSubmission.toSubmissionDTO()
    }

    @Transactional
    fun updateSubmissionResult(submissionId: Long, score: Float): SubmissionDto {
        logger.info("Start: updateSubmissionResult | submissionId=$submissionId, score=$score")

        if (score < 0f || score > 100f) {
            throw BadRequestException("Score must be between 0 and 100")
        }

        val submission = submissionRepository.findByIdOrNull(submissionId)
            ?: throw SubmissionNotFoundException("Submission not found with ID: $submissionId")

        submission.score = score
        submission.status = determineStatus(score)

        val updatedSubmission = submissionRepository.save(submission)
        logger.info("Submission result updated | submissionId=$submissionId, status=${submission.status}")

        return updatedSubmission.toSubmissionDTO()
    }

    fun getSubmissionsByProblemAndUser(problemId: Long): List<SubmissionDto> {
        logger.info("Start: getSubmissionsByProblemAndUser | problemId=$problemId")

        val appUser = getAuthenticatedUser()

        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("Problem not found with ID: $problemId")

        val submissions = submissionRepository.findAllByProblemIdAndAppUserId(problemId, appUser.id)
        logger.info("Retrieved submissions | count=${submissions?.size ?: 0}")

        return mapSubmissionListEntityToSubmissionListDTO(submissions ?: emptyList())
    }

    fun getSubmissionById(submissionId: Long): SubmissionDto {
        logger.info("Start: getSubmissionById | submissionId=$submissionId")

        val submission = submissionRepository.findByIdOrNull(submissionId)
            ?: throw SubmissionNotFoundException("Submission not found with ID: $submissionId")

        return submission.toSubmissionDTO()
    }

    @Transactional
    fun deleteAllSubmissionsByProblemAndUser(problemId: Long) {
        logger.info("Start: deleteAllSubmissionsByProblemAndUser | problemId=$problemId")

        val appUser = getAuthenticatedUser()

        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("Problem not found with ID: $problemId")

        val deletedCount = submissionRepository.countByProblemIdAndAppUserId(problemId, appUser.id)
        submissionRepository.deleteAllByProblemIdAndAppUserId(problemId, appUser.id)

        logger.info("Deleted submissions | count=$deletedCount, userId=${appUser.id}, problemId=$problemId")
    }

    @Transactional
    fun deleteSubmission(submissionId: Long) {
        logger.info("Start: deleteSubmission | submissionId=$submissionId")

        val submission = submissionRepository.findByIdOrNull(submissionId)
            ?: throw SubmissionNotFoundException("Submission not found with ID: $submissionId")

        submissionRepository.delete(submission)
        logger.info("Submission deleted | submissionId=$submissionId")
    }

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