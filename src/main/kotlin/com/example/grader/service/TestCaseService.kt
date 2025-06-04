package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.TestCaseDto
import com.example.grader.dto.RequstResponse.TestCaseRequest
import com.example.grader.entity.TestCase
import com.example.grader.error.ProblemNotFoundException
import com.example.grader.error.TestCaseNotFoundException
import com.example.grader.repository.ProblemRepository
import com.example.grader.repository.TestCaseRepository
import com.example.grader.util.ResponseUtil
import com.example.grader.util.mapTestCaseListEntityToTestCaseListDTO
import com.example.grader.util.toTestCaseDTO
import jdk.incubator.vector.VectorOperators.Test
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TestCaseService(
    private val testCaseRepository: TestCaseRepository,
    private val problemRepository: ProblemRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun createTestCase(testCaseRequest: TestCaseRequest, problemId: Long): ApiResponse<TestCaseDto> {

        val problem = problemRepository.findById(problemId).orElseThrow {
            ProblemNotFoundException("No Problem found with ID $problemId")
        }

        val testCase = TestCase(
            problem = problem,
            input = testCaseRequest.input,
            output = testCaseRequest.output,
            type = testCaseRequest.type
        )

        val savedTestCase = testCaseRepository.save(testCase)

        return ResponseUtil.created(
            message = "Added TestCase successfully",
            data = savedTestCase.toTestCaseDTO(),
            metadata = null
        )

    }

    fun getTestCasesByProblemId(problemId: Long): ApiResponse<List<TestCaseDto>> {

        val testCaseList = testCaseRepository.findByProblemId(problemId)
            ?: throw TestCaseNotFoundException("No TestCase found with ID $problemId")
        val testCaseListDto = mapTestCaseListEntityToTestCaseListDTO(testCaseList)

        return ResponseUtil.success(
            message = "Fetched all TestCases successfully",
            data = testCaseListDto,
            metadata = null
        )

    }

    fun getTestCaseById(id: Long): ApiResponse<TestCaseDto> {

        val testCase = testCaseRepository.findByIdOrNull(id)
            ?: throw TestCaseNotFoundException("No TestCase found with ID $id")

        return ResponseUtil.success(
            message = "Fetched TestCase successfully",
            data = testCase.toTestCaseDTO(),
            metadata = null
        )

    }

    fun updateTestCase(id: Long, testCaseRequest: TestCaseRequest, problemId: Long): ApiResponse<TestCaseDto> {

        val existingTestCase = testCaseRepository.findByIdOrNull(id)
            ?: throw TestCaseNotFoundException("No TestCase found with ID $id")

        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("No Problem found with ID $problemId")

        existingTestCase.problem = problem
        existingTestCase.input = testCaseRequest.input
        existingTestCase.output = testCaseRequest.output
        existingTestCase.type = testCaseRequest.type
        existingTestCase.updatedAt = Instant.now()

        val savedTestCase = testCaseRepository.save(existingTestCase)

        return ResponseUtil.success(
            message = "Updated TestCase successfully",
            data = savedTestCase.toTestCaseDTO(),
            metadata = null
        )

    }

    fun deleteTestCase(id: Long): ApiResponse<Unit> {

        val testCase = testCaseRepository.findByIdOrNull(id)
            ?: throw TestCaseNotFoundException("No TestCase found with ID $id")

        testCaseRepository.delete(testCase)

        return ResponseUtil.success(
            message = "Deleted TestCase successfully",
            data = Unit,
            metadata = null
        )

    }
}