package com.example.grader.service

import com.example.grader.dto.TestCaseDto
import com.example.grader.dto.RequestResponse.TestCaseRequest
import com.example.grader.entity.Problem
import com.example.grader.entity.TestCase
import com.example.grader.error.BadRequestException
import com.example.grader.error.ProblemNotFoundException
import com.example.grader.error.TestCaseNotFoundException
import com.example.grader.repository.ProblemRepository
import com.example.grader.repository.TestCaseRepository
import com.example.grader.util.mapTestCaseListEntityToTestCaseListDTO
import com.example.grader.util.toTestCaseDTO
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class TestCaseService(
    private val testCaseRepository: TestCaseRepository,
    private val problemRepository: ProblemRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun createTestCase(testCaseRequest: TestCaseRequest, problemId: Long): TestCaseDto {
        validateTestCaseRequest(testCaseRequest)
        val problem = findProblemById(problemId)

        val testCase = TestCase(
            problem = problem,
            input = testCaseRequest.input.trim(),
            output = testCaseRequest.output.trim(),
            type = testCaseRequest.type
        )

        val savedTestCase = testCaseRepository.save(testCase)

        logger.info("Created test case with ID: ${savedTestCase.id} for problem ID: $problemId")

        return savedTestCase.toTestCaseDTO()
    }


    fun getTestCasesByProblemId(problemId: Long): List<TestCaseDto> {
        // Verify problem exists first
        findProblemById(problemId)

        val testCases = testCaseRepository.findByProblemId(problemId)

        return mapTestCaseListEntityToTestCaseListDTO(testCases)
    }

    fun getTestCaseById(id: Long): TestCaseDto {
        val testCase = findTestCaseById(id)

        return testCase.toTestCaseDTO()
    }

    fun updateTestCase(id: Long, problemId: Long, testCaseRequest: TestCaseRequest): TestCaseDto {
        validateTestCaseRequest(testCaseRequest)

        val existingTestCase = findTestCaseById(id)
        val problem = findProblemById(problemId)

        existingTestCase.apply {
            this.problem = problem
            input = testCaseRequest.input.trim()
            output = testCaseRequest.output.trim()
            type = testCaseRequest.type
            updatedAt = Instant.now()
        }

        val savedTestCase = testCaseRepository.save(existingTestCase)

        logger.info("Updated test case with ID: $id")

        return savedTestCase.toTestCaseDTO()
    }

    @Transactional
    fun deleteTestCase(id: Long) {
        val testCase = findTestCaseById(id)

        testCaseRepository.delete(testCase)

        logger.info("Deleted test case with ID: $id")
    }

    private fun validateTestCaseRequest(request: TestCaseRequest) {
        when {
            request.input.isBlank() -> throw BadRequestException("Test case input cannot be blank")
            request.output.isBlank() -> throw BadRequestException("Test case output cannot be blank")
            request.input.length > MAX_INPUT_LENGTH ->
                throw BadRequestException("Test case input cannot exceed $MAX_INPUT_LENGTH characters")
            request.output.length > MAX_OUTPUT_LENGTH ->
                throw BadRequestException("Test case output cannot exceed $MAX_OUTPUT_LENGTH characters")
        }
    }

    private fun findProblemById(problemId: Long): Problem {
        return problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("No Problem found with ID $problemId")
    }

    private fun findTestCaseById(id: Long): TestCase {
        return testCaseRepository.findByIdOrNull(id)
            ?: throw TestCaseNotFoundException("No TestCase found with ID $id")
    }

    companion object {
        private const val MAX_INPUT_LENGTH = 10000 // Adjust as needed
        private const val MAX_OUTPUT_LENGTH = 10000 // Adjust as needed
    }
}