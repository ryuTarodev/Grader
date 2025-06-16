package com.example.grader.service

import com.example.grader.dto.RequesttResponse.TestCaseRequest
import com.example.grader.entity.Difficulty
import com.example.grader.entity.Problem
import com.example.grader.entity.TestCase
import com.example.grader.entity.Type
import com.example.grader.error.BadRequestException
import com.example.grader.error.ProblemNotFoundException
import com.example.grader.error.TestCaseNotFoundException
import com.example.grader.repository.ProblemRepository
import com.example.grader.repository.TestCaseRepository
import com.example.grader.util.toTestCaseDTO
import io.mockk.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant

class TestCaseServiceTest {

    private val testCaseRepository: TestCaseRepository = mockk(relaxed = true)
    private val problemRepository: ProblemRepository = mockk()
    private lateinit var testCaseService: TestCaseService

    private val mockProblem = Problem(id = 1L, title = "Test", difficulty = Difficulty.EASY)
    private val mockTestCase = TestCase(
        id = 10L,
        input = "1 2",
        output = "3",
        type = Type.PUBLIC,
        problem = mockProblem,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )

    @BeforeEach
    fun setUp() {
        testCaseService = TestCaseService(testCaseRepository, problemRepository)
    }

    @Test
    fun `createTestCase should save and return DTO`() {
        val req = TestCaseRequest(" 1 2 ", " 3 ", Type.PUBLIC)
        every { problemRepository.findByIdOrNull(1L) } returns mockProblem
        every { testCaseRepository.save(any()) } returns mockTestCase

        val result = testCaseService.createTestCase(req, 1L)

        assertEquals("1 2", result.input)
        assertEquals("3", result.output)
        assertEquals(Type.PUBLIC, result.type)
        verify { testCaseRepository.save(any()) }
    }

    @Test
    fun `createTestCase should throw when input is blank`() {
        val req = TestCaseRequest(" ", "3", Type.PUBLIC)
        val ex = assertThrows<BadRequestException> {
            testCaseService.createTestCase(req, 1L)
        }
        assertEquals("Test case input cannot be blank", ex.message)
    }

    @Test
    fun `createTestCase should throw when output is blank`() {
        val req = TestCaseRequest("1 2", " ", Type.PUBLIC)
        val ex = assertThrows<BadRequestException> {
            testCaseService.createTestCase(req, 1L)
        }
        assertEquals("Test case output cannot be blank", ex.message)
    }

    @Test
    fun `createTestCase should throw when problem not found`() {
        val req = TestCaseRequest("1 2", "3", Type.PUBLIC)
        every { problemRepository.findByIdOrNull(1L) } returns null

        val ex = assertThrows<ProblemNotFoundException> {
            testCaseService.createTestCase(req, 1L)
        }
        assertEquals("No Problem found with ID 1", ex.message)
    }

    @Test
    fun `getTestCasesByProblemId should return list of DTOs`() {
        every { problemRepository.findByIdOrNull(1L) } returns mockProblem
        every { testCaseRepository.findByProblemId(1L) } returns listOf(mockTestCase)

        val result = testCaseService.getTestCasesByProblemId(1L)

        assertEquals(1, result.size)
        assertEquals("1 2", result[0].input)
    }

    @Test
    fun `getTestCasesByProblemId should throw if problem not found`() {
        every { problemRepository.findByIdOrNull(1L) } returns null

        val ex = assertThrows<ProblemNotFoundException> {
            testCaseService.getTestCasesByProblemId(1L)
        }

        assertEquals("No Problem found with ID 1", ex.message)
    }

    @Test
    fun `getTestCaseById should return DTO`() {
        every { testCaseRepository.findByIdOrNull(10L) } returns mockTestCase

        val result = testCaseService.getTestCaseById(10L)

        assertEquals("1 2", result.input)
        assertEquals("3", result.output)
    }

    @Test
    fun `getTestCaseById should throw if not found`() {
        every { testCaseRepository.findByIdOrNull(10L) } returns null

        val ex = assertThrows<TestCaseNotFoundException> {
            testCaseService.getTestCaseById(10L)
        }

        assertEquals("No TestCase found with ID 10", ex.message)
    }

    @Test
    fun `updateTestCase should update and return DTO`() {
        val req = TestCaseRequest(" 2 3 ", " 5 ", Type.PUBLIC)
        every { testCaseRepository.findByIdOrNull(10L) } returns mockTestCase
        every { problemRepository.findByIdOrNull(1L) } returns mockProblem
        every { testCaseRepository.save(any()) } returns mockTestCase.copy(input = "2 3", output = "5")

        val result = testCaseService.updateTestCase(10L, 1L, req)

        assertEquals("2 3", result.input)
        assertEquals("5", result.output)
        verify { testCaseRepository.save(any()) }
    }

    @Test
    fun `updateTestCase should throw if test case not found`() {
        val req = TestCaseRequest(" 2 3 ", " 5 ", Type.PUBLIC)
        every { testCaseRepository.findByIdOrNull(10L) } returns null

        val ex = assertThrows<TestCaseNotFoundException> {
            testCaseService.updateTestCase(10L, 1L, req)
        }

        assertEquals("No TestCase found with ID 10", ex.message)
    }

    @Test
    fun `updateTestCase should throw if problem not found`() {
        val req = TestCaseRequest(" 2 3 ", " 5 ", Type.PUBLIC)
        every { testCaseRepository.findByIdOrNull(10L) } returns mockTestCase
        every { problemRepository.findByIdOrNull(1L) } returns null

        val ex = assertThrows<ProblemNotFoundException> {
            testCaseService.updateTestCase(10L, 1L, req)
        }

        assertEquals("No Problem found with ID 1", ex.message)
    }

    @Test
    fun `deleteTestCase should delete test case`() {
        every { testCaseRepository.findByIdOrNull(10L) } returns mockTestCase
        every { testCaseRepository.delete(mockTestCase) } just Runs

        testCaseService.deleteTestCase(10L)

        verify { testCaseRepository.delete(mockTestCase) }
    }

    @Test
    fun `deleteTestCase should throw if not found`() {
        every { testCaseRepository.findByIdOrNull(10L) } returns null

        val ex = assertThrows<TestCaseNotFoundException> {
            testCaseService.deleteTestCase(10L)
        }

        assertEquals("No TestCase found with ID 10", ex.message)
    }
}