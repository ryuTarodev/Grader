package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.TestCaseDto
import com.example.grader.dto.RequstResponse.UpdateTestCaseRequest
import com.example.grader.entity.TestCase
import com.example.grader.error.ProblemNotFoundException
import com.example.grader.error.TestCaseNotFoundException
import com.example.grader.repository.ProblemRepository
import com.example.grader.repository.TestCaseRepository
import com.example.grader.util.ResponseUtil
import com.example.grader.util.mapTestCaseListEntityToTestCaseListDTO
import com.example.grader.util.toTestCaseDTO
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TestCaseService (
    private val testCaseRepository: TestCaseRepository,
    private val problemRepository: ProblemRepository
){
    private val logger = LoggerFactory.getLogger(javaClass)

    fun createTestCase(testCaseRequest: UpdateTestCaseRequest): ApiResponse<TestCaseDto> {
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

    fun getTestCasesByProblemId(problemId: Long): ApiResponse<List<TestCaseDto>>  {
        return try {
            val testCaseList = testCaseRepository.findByProblemId(problemId)
            val testCaseListDto = mapTestCaseListEntityToTestCaseListDTO(testCaseList)
            ResponseUtil.success(
                message = "get all TestCase list successfully",
                data = testCaseListDto,
                metadata = null
            )
        }catch (e: Exception) {
            logger.error("An unexpected error occurred: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = emptyList()
            )
        }


    }

    fun updateTestCase(id: Long, testCaseRequest: UpdateTestCaseRequest): ApiResponse<TestCaseDto> {
        return try {
            val existingTestCase = testCaseRepository.findByIdOrNull(id)
                ?: throw TestCaseNotFoundException("No TestCase found with ID $id")

            val problem = problemRepository.findByIdOrNull(testCaseRequest.problemId)
                ?: throw ProblemNotFoundException("No Problem found with ID ${testCaseRequest.problemId}")

            existingTestCase.problem = problem
            existingTestCase.input = testCaseRequest.input
            existingTestCase.output = testCaseRequest.output
            existingTestCase.type = testCaseRequest.type
            existingTestCase.updatedAt = Instant.now()

            val savedTestCase = testCaseRepository.save(existingTestCase)
            val testCaseDTO = savedTestCase.toTestCaseDTO()

            ResponseUtil.success(
                message = "Updated TestCase successfully",
                data = testCaseDTO,
                metadata = null
            )
        } catch (e: TestCaseNotFoundException) {
            logger.error("TestCaseNotFound: ${e.message}")
            ResponseUtil.notFound(
                message = "Invalid request: ${e.message}",
                data = TestCaseDto()
            )
        } catch (e: ProblemNotFoundException) {
            logger.error("ProblemNotFound: ${e.message}")
            ResponseUtil.notFound(
                message = "Invalid request: ${e.message}",
                data = TestCaseDto()
            )
        } catch (e: Exception) {
            logger.error("Unexpected error: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = TestCaseDto()
            )
        }
    }

    fun deleteTestCase(id: Long): ApiResponse<Unit> {
        return try {
            val testCase = testCaseRepository.findByIdOrNull(id)
                ?: throw TestCaseNotFoundException("No TestCase found with ID $id")

            testCaseRepository.delete(testCase)

            ResponseUtil.success(
                message = "Deleted TestCase successfully",
                data = Unit,
                metadata = null
            )
        } catch (e: TestCaseNotFoundException) {
            logger.error("TestCaseNotFound: ${e.message}")
            ResponseUtil.notFound(
                message = "Invalid request: ${e.message}",
                data = Unit
            )
        } catch (e: Exception) {
            logger.error("Unexpected error: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = Unit
            )
        }
    }
}
