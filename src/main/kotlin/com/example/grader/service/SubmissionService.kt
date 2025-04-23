package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.RequstResponse.SubmissionRequest
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

    fun createSubmission(problemId: Long, appUserId: Long, code: String): ApiResponse<SubmissionDto> {
        return try {
            val testCases = testCaseRepository.findByProblemId(problemId)
                ?: throw TestCaseNotFoundException("TestCaseNotFound")
            val appUser = appUserRepository.findByIdOrNull(appUserId)
                ?: throw UserNotFoundException("User Not Found")
            val problem = problemRepository.findByIdOrNull(problemId)
                ?: throw ProblemNotFoundException("Problem not found")

            val submission = Submission(
                appUser = appUser,
                problem = problem,
                code = code
            )

            val savedSubmission = submissionRepository.save(submission)
            val submissionDto = savedSubmission.toSubmissionDTO()
            val message = SubmissionSendMessage(
                submissionDto,
                mapTestCaseListEntityToTestCaseListDTO(testCases)
            )

            logger.info("Sending code: ${submission.code}") // Debug log
            submissionProducer.sendSubmission(message)

            ResponseUtil.success(
                message = "sending message to RabbitMQ",
                data = submissionDto,
                metadata = null
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred while creating submission", e)
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = SubmissionDto(status = Status.REJECTED)
            )
        }
    }

    fun updateSubmission(submissionId: Long, score: Float = 0f): ApiResponse<SubmissionDto> {
        return try {
            val existingSubmission = submissionRepository.findByIdOrNull(submissionId)
                ?: throw SubmissionNotFoundException("No submission found with ID $submissionId")

            existingSubmission.score = score
            existingSubmission.status = Status.ACCEPTED
            val savedSubmission = submissionRepository.save(existingSubmission)
            val submissionDto = savedSubmission.toSubmissionDTO()

            ResponseUtil.success(
                message = "Submission updated successfully.",
                data = submissionDto,
                metadata = null
            )
        } catch (e: Exception) {
            logger.error("Error updating submission", e)
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = SubmissionDto()
            )
        }
    }

    fun getSubmissionByProblemIdAndAppUserId(problemId: Long, appUserId: Long): ApiResponse<List<SubmissionDto>> {
        return try {
            val submissions = submissionRepository.findAllByProblemIdAndAppUserId(problemId, appUserId)
                ?: throw SubmissionNotFoundException("Submission not found")
            val submissionDtoList = mapSubmissionListEntityToSubmissionListDTO(submissions)

            ResponseUtil.success(
                message = "List all submissions",
                data = submissionDtoList,
                metadata = null
            )
        } catch (e: SubmissionNotFoundException) {
            logger.error("SubmissionNotFound: ${e.message}")
            ResponseUtil.notFound(
                message = "Invalid request: ${e.message}",
                data = emptyList()
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred while getting submissions", e)
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = emptyList()
            )
        }
    }

    @Transactional
    fun deleteAllSubmissions(problemId: Long, appUserId: Long): ApiResponse<Unit> {
        return try {
            submissionRepository.deleteAllByProblemIdAndAppUserId(problemId, appUserId)
            ResponseUtil.success(
                message = "Remove Successfully",
                data = Unit,
                metadata = null
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred while deleting submissions", e)
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = Unit
            )
        }
    }
}