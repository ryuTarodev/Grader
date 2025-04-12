package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.AppUserDto
import com.example.grader.dto.TestCaseDto
import com.example.grader.dto.TestCaseRequest
import com.example.grader.entity.TestCase
import com.example.grader.error.ProblemNotFoundException
import com.example.grader.error.UserNotFoundException
import com.example.grader.repository.ProblemRepository
import com.example.grader.repository.TestCaseRepository
import com.example.grader.util.ResponseUtil
import com.example.grader.util.toTestCaseDTO
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class TestCaseService (
    private val testCaseRepository: TestCaseRepository,
    private val problemRepository: ProblemRepository
){
    private val logger = LoggerFactory.getLogger(javaClass)

    fun createTestCase(testCaseRequest: TestCaseRequest): ApiResponse<TestCaseDto> {
        return try {
            val problem = problemRepository.findById(testCaseRequest.problemId).orElseThrow {
                ProblemNotFoundException("No User found with ID $testCaseRequest.problemId")
            }

            val testCase = TestCase(
                problem = problem,
                input = testCaseRequest.input,
                output = testCaseRequest.output,
                type = testCaseRequest.type
            )
            val savedTestCase = testCaseRepository.save(testCase)
            val testCaseDTO = savedTestCase.toTestCaseDTO()
            ResponseUtil.created(
                    message = "added TestCase successfully",
                    data = testCaseDTO,
                    metadata = null
            )
        }catch (e: ProblemNotFoundException) {
            logger.error("ProblemNotFound: ${e.message}")
            ResponseUtil.notFound(
                message = "Invalid request: ${e.message}",
                data = TestCaseDto()
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = TestCaseDto()
            )
        }

    }
}