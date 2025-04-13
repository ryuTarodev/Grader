package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.ProblemDto
import com.example.grader.dto.RequstResponse.SubmissionRequest
import com.example.grader.dto.SubmissionDto
import com.example.grader.dto.SubmissionMessage
import com.example.grader.entity.Problem
import com.example.grader.entity.Submission
import com.example.grader.error.ProblemNotFoundException
import com.example.grader.error.TestCaseNotFoundException
import com.example.grader.error.UserNotFoundException
import com.example.grader.repository.AppUserRepository
import com.example.grader.repository.ProblemRepository
import com.example.grader.repository.SubmissionRepository
import com.example.grader.repository.TestCaseRepository
import com.example.grader.util.ResponseUtil
import com.example.grader.util.mapTestCaseListEntityToTestCaseListDTO
import com.example.grader.util.toSubmissionDTO
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class SubmissionService(
    private val problemRepository: ProblemRepository,
    private val appUserRepository: AppUserRepository,
    private val submissionRepository: SubmissionRepository,
    private val testCaseRepository: TestCaseRepository,
    private val rabbitTemplate: RabbitTemplate,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${rabbitmq.exchange.name}")
    private lateinit var exchangeName: String

    @Value("\${rabbitmq.routing.key}")
    private lateinit var routingKey: String


    fun createSubmission(submissionRequest: SubmissionRequest): ApiResponse<SubmissionDto> {
        return try {
            val testCases = testCaseRepository.findByProblemId(submissionRequest.problemId)
                ?: throw TestCaseNotFoundException("TestCaseNotFound")
            val appUser = appUserRepository.findByIdOrNull(submissionRequest.appUserId) ?: throw UserNotFoundException("User Not Found")
            val problem = problemRepository.findByIdOrNull(submissionRequest.problemId) ?: throw ProblemNotFoundException("Problem not found")
            val submission = Submission(
                appUser = appUser,
                problem = problem,
                code = submissionRequest.code,
            )

            val savedSubmission = submissionRepository.save(submission)
            val sumissionDto = submission.toSubmissionDTO()

            val message = SubmissionMessage(sumissionDto, mapTestCaseListEntityToTestCaseListDTO(testCases))

            rabbitTemplate.convertAndSend(exchangeName, routingKey, message)
            logger.info("Sent submission ${savedSubmission.id} to RabbitMQ")

            ResponseUtil.success(
                message = "sending message to RabbitMQ",
                data = sumissionDto,
                metadata = null
            )
        }catch (e: Exception) {
            logger.error("An unexpected error occurred while updating problem", e)
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = SubmissionDto()
            )
        }

    }
}