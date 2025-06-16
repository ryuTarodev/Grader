package com.example.grader.service

import com.example.grader.dto.RequesttResponse.SubmissionRequest
import com.example.grader.dto.SubmissionDto
import com.example.grader.dto.SubmissionSendMessage
import com.example.grader.entity.*
import com.example.grader.error.*
import com.example.grader.repository.*
import com.example.grader.util.mapSubmissionListEntityToSubmissionListDTO
import com.example.grader.util.mapTestCaseListEntityToTestCaseListDTO
import io.mockk.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.data.repository.findByIdOrNull

class SubmissionServiceTest {

    private val problemRepository = mockk<ProblemRepository>()
    private val appUserRepository = mockk<AppUserRepository>()
    private val submissionRepository = mockk<SubmissionRepository>(relaxed = true)
    private val testCaseRepository = mockk<TestCaseRepository>()
    private val submissionProducer = mockk<SubmissionProducer>(relaxed = true)

    private lateinit var submissionService: SubmissionService

    private val user = AppUser(id = 1, appUsername = "John")
    private val problem = Problem(id = 1, title = "FizzBuzz")
    private val testCases = listOf(TestCase(id = 1, input = "1", output = "1", problem = problem))
    private val submissionRequest = SubmissionRequest(code = "print(1)", language = "Python")

    @BeforeEach
    fun setUp() {
        submissionService = SubmissionService(
            problemRepository,
            appUserRepository,
            submissionRepository,
            testCaseRepository,
            submissionProducer
        )
    }

    @Test
    fun `createSubmission should save and send to RabbitMQ`() {
        every { appUserRepository.findByIdOrNull(1) } returns user
        every { problemRepository.findByIdOrNull(1) } returns problem
        every { testCaseRepository.findByProblemId(1) } returns testCases
        every { submissionRepository.save(any()) } answers { firstArg() }

        val result = submissionService.createSubmission(1, 1, submissionRequest)

        assertEquals("Python", result.language)
        verify { submissionProducer.sendSubmission(any()) }
    }

    @Test
    fun `createSubmission should throw exception if user not found`() {
        every { appUserRepository.findByIdOrNull(1) } returns null
        val ex = assertThrows<UserNotFoundException> {
            submissionService.createSubmission(1, 1, submissionRequest)
        }
        assertTrue(ex.message!!.contains("User not found"))
    }

    @Test
    fun `createSubmission should throw exception if problem not found`() {
        every { appUserRepository.findByIdOrNull(1) } returns user
        every { problemRepository.findByIdOrNull(1) } returns null

        val ex = assertThrows<ProblemNotFoundException> {
            submissionService.createSubmission(1, 1, submissionRequest)
        }
        assertTrue(ex.message!!.contains("Problem not found"))
    }

    @Test
    fun `createSubmission should throw if no test cases`() {
        every { appUserRepository.findByIdOrNull(1) } returns user
        every { problemRepository.findByIdOrNull(1) } returns problem
        every { testCaseRepository.findByProblemId(1) } returns emptyList()

        val ex = assertThrows<TestCaseNotFoundException> {
            submissionService.createSubmission(1, 1, submissionRequest)
        }
        assertTrue(ex.message!!.contains("No test cases"))
    }

    @Test
    fun `updateSubmissionFields should update submission`() {
        val submission = Submission(id = 1, appUser =  user, problem =   problem, code = "code", language =  "Python", status =  Status.PENDING )
        every { submissionRepository.findByIdOrNull(1) } returns submission
        every { submissionRepository.save(any()) } answers { firstArg() }

        val result = submissionService.updateSubmissionFields(1, submissionRequest)
        assertEquals(Status.PENDING, result.status)
    }

    @Test
    fun `updateSubmissionFields should throw if already processed`() {
        val processedSubmission = Submission(
            id = 1,
            appUser = user,
            problem = problem,
            code = "code",
            language = "Python",
            status = Status.ACCEPTED
        )

        every { submissionRepository.findByIdOrNull(1) } returns processedSubmission
        every { submissionRepository.save(any()) } returns processedSubmission

        assertThrows<BadRequestException> {
            submissionService.updateSubmissionFields(1, submissionRequest)
        }
    }

    @Test
    fun `updateSubmissionResult should update score and status`() {
        val submission = Submission(id = 1, appUser =  user, problem =   problem, code = "code", language =  "Python", status =  Status.PENDING )
        every { submissionRepository.findByIdOrNull(1) } returns submission
        every { submissionRepository.save(any()) } answers { firstArg() }

        val result = submissionService.updateSubmissionResult(1, 100f)
        assertEquals(Status.ACCEPTED, result.status)
    }

    @Test
    fun `updateSubmissionResult should throw if score is invalid`() {
        assertThrows<BadRequestException> {
            submissionService.updateSubmissionResult(1, 101f)
        }
    }

    @Test
    fun `getSubmissionsByProblemAndUser should return submissions`() {
        every { problemRepository.findByIdOrNull(1) } returns problem
        every { appUserRepository.findByIdOrNull(1) } returns user
        every { submissionRepository.findAllByProblemIdAndAppUserId(1, 1) } returns listOf()

        val result = submissionService.getSubmissionsByProblemAndUser(1, 1)
        assertEquals(0, result.size)
    }

    @Test
    fun `getSubmissionById should return submission`() {
        val submission = Submission(id = 1, appUser =  user, problem =   problem, code = "code", language =  "Python", status =  Status.PENDING )
        every { submissionRepository.findByIdOrNull(1) } returns submission

        val result = submissionService.getSubmissionById(1)
        assertEquals("Python", result.language)
    }

    @Test
    fun `getSubmissionsByUser should return submissions`() {
        every { appUserRepository.findByIdOrNull(1) } returns user
        every { submissionRepository.findAllByAppUserId(1) } returns listOf()

        val result = submissionService.getSubmissionsByUser(1)
        assertEquals(0, result.size)
    }

    @Test
    fun `getSubmissionsByProblem should return submissions`() {
        every { problemRepository.findByIdOrNull(1) } returns problem
        every { submissionRepository.findAllByProblemId(1) } returns listOf()

        val result = submissionService.getSubmissionsByProblem(1)
        assertEquals(0, result.size)
    }

    @Test
    fun `deleteAllSubmissionsByProblemAndUser should delete submissions`() {
        every { problemRepository.findByIdOrNull(1) } returns problem
        every { appUserRepository.findByIdOrNull(1) } returns user
        every { submissionRepository.countByProblemIdAndAppUserId(1, 1) } returns 2
        every { submissionRepository.deleteAllByProblemIdAndAppUserId(1, 1) } just Runs

        submissionService.deleteAllSubmissionsByProblemAndUser(1, 1)
    }

    @Test
    fun `deleteSubmission should delete by ID`() {
        val submission = Submission(id = 1, appUser =  user, problem =   problem, code = "code", language =  "Python", status =  Status.PENDING )
        every { submissionRepository.findByIdOrNull(1) } returns submission
        every { submissionRepository.delete(any()) } just Runs

        submissionService.deleteSubmission(1)
    }
}