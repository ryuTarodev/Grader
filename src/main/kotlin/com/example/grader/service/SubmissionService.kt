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
    companion object {
        private val EMPTY_SUBMISSION = SubmissionDto()
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    fun createSubmission(problemId: Long, appUserId: Long, code: String, language: String): ApiResponse<SubmissionDto> {
        val testCases = testCaseRepository.findByProblemId(problemId)
            ?: throw TestCaseNotFoundException("TestCase not found")

        val appUser = appUserRepository.findByIdOrNull(appUserId)
            ?: throw UserNotFoundException("User $appUserId not found")

        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("Problem $problemId not found")

        val submission = Submission(
            appUser = appUser,
            problem = problem,
            code = code,
            language = language
        )

        val savedSubmission = submissionRepository.save(submission)

        val submissionDto = savedSubmission.toSubmissionDTO()
        val message = SubmissionSendMessage(
            submissionDto,
            mapTestCaseListEntityToTestCaseListDTO(testCases)
        )

        logger.info("Sending submission ${savedSubmission.id} to RabbitMQ")
        submissionProducer.sendSubmission(message)

        return ResponseUtil.success(
            message = "Submission sent to RabbitMQ.",
            data = submissionDto,
            metadata = null
        )
    }

    @Transactional
    fun updateSubmissionFields(submissionId: Long, request: SubmissionRequest): ApiResponse<SubmissionDto> {
        val submission = submissionRepository.findByIdOrNull(submissionId)
            ?: throw SubmissionNotFoundException("Submission $submissionId not found")

        submission.code = request.code
        submission.language = request.language

        val updated = submissionRepository.save(submission)
        return ResponseUtil.success("Submission updated", updated.toSubmissionDTO(), metadata = null)
    }

    @Transactional
    fun updateSubmissionResult(submissionId: Long, score: Float): ApiResponse<SubmissionDto> {
        val submission = submissionRepository.findByIdOrNull(submissionId)
            ?: throw SubmissionNotFoundException("Submission $submissionId not found")

        submission.score = score
        submission.status = if (score <= 0f) Status.REJECTED else Status.ACCEPTED

        val updated = submissionRepository.save(submission)
        return ResponseUtil.success("Submission result updated", updated.toSubmissionDTO(), metadata = null)
    }

    fun getSubmissionByProblemIdAndAppUserId(problemId: Long, appUserId: Long): ApiResponse<List<SubmissionDto>> {
        val submissions = submissionRepository.findAllByProblemIdAndAppUserId(problemId, appUserId)
            ?: throw SubmissionNotFoundException("Submission not found")

        val submissionDtoList = mapSubmissionListEntityToSubmissionListDTO(submissions)

        return ResponseUtil.success(
            message = "List all submissions",
            data = submissionDtoList,
            metadata = null
        )
    }

    @Transactional
    fun deleteAllSubmissions(problemId: Long, appUserId: Long): ApiResponse<Unit> {
        submissionRepository.deleteAllByProblemIdAndAppUserId(problemId, appUserId)

        return ResponseUtil.success(
            message = "Remove Successfully",
            data = Unit,
            metadata = null
        )
    }
}