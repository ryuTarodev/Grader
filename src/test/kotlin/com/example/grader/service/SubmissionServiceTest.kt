package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.RequstResponse.SubmissionRequest
import com.example.grader.dto.SubmissionDto
import com.example.grader.dto.SubmissionSendMessage
import com.example.grader.entity.*
import com.example.grader.repository.*
import com.example.grader.util.ResponseUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
class SubmissionServiceTest {

    @Mock lateinit var problemRepository: ProblemRepository
    @Mock lateinit var appUserRepository: AppUserRepository
    @Mock lateinit var submissionRepository: SubmissionRepository
    @Mock lateinit var testCaseRepository: TestCaseRepository
    @Mock lateinit var submissionProducer: SubmissionProducer

    @InjectMocks
    lateinit var submissionService: SubmissionService

    private lateinit var user: AppUser
    private lateinit var problem: Problem
    private lateinit var testCase: TestCase
    private lateinit var submission: Submission

    @BeforeEach
    fun setUp() {
        user = AppUser(id = 1L, appUsername = "john", clientPassword  = "pass")
        problem = Problem(id = 1L, title = "Test Problem", difficulty = Difficulty.EASY)
        testCase = TestCase(id = 1L, input = "1 2", output = "3", problem = problem)

        submission = Submission(
            id = 1L,
            code = "print()",
            language = "python",
            appUser = user,
            problem = problem
        )
    }

    @Test
    fun `createSubmission should save and send submission`() {
        `when`(appUserRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(problemRepository.findById(1L)).thenReturn(Optional.of(problem))
        `when`(testCaseRepository.findByProblemId(1L)).thenReturn(listOf(testCase))
        `when`(submissionRepository.save(any<Submission>() ?: submission)).thenReturn(submission)

        val response = submissionService.createSubmission(1L, 1L, "print()", "python")

        assertNotNull(response.data)
        assertEquals("Submission sent to RabbitMQ.", response.message)

    }
    @Test
    fun `getSubmissionByProblemIdAndAppUserId should return list`() {
        `when`(submissionRepository.findAllByProblemIdAndAppUserId(1L, 1L)).thenReturn(listOf(submission))

        val response = submissionService.getSubmissionByProblemIdAndAppUserId(1L, 1L)

        assertEquals(1, response.data?.size)
        assertEquals("List all submissions", response.message)
    }

    @Test
    fun `updateSubmissionFields should update code and language`() {
        val updatedRequest = SubmissionRequest(code = "updated()", language = "java")
        `when`(submissionRepository.findById(1L)).thenReturn(Optional.of(submission))
        `when`(submissionRepository.save(any<Submission>() ?: submission)).thenReturn(submission)

        val response = submissionService.updateSubmissionFields(1L, updatedRequest)

        assertEquals("Submission updated", response.message)
        assertEquals("java", response.data?.language)
    }

    @Test
    fun `updateSubmissionResult should set status ACCEPTED if score positive`() {
        `when`(submissionRepository.findById(1L)).thenReturn(Optional.of(submission))
        `when`(submissionRepository.save(any<Submission>() ?: submission)).thenReturn(submission)

        val response = submissionService.updateSubmissionResult(1L, 80f)

        assertEquals("Submission result updated", response.message)
        assertEquals(Status.ACCEPTED, response.data?.status)
    }



    @Test
    fun `deleteAllSubmissions should return success`() {
        val response = submissionService.deleteAllSubmissions(1L, 1L)

        assertEquals("Remove Successfully", response.message)
        verify(submissionRepository, times(1)).deleteAllByProblemIdAndAppUserId(1L, 1L)
    }
}