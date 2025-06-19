package com.example.grader.service

import com.example.grader.dto.RequestResponse.ProblemRequest
import com.example.grader.entity.Difficulty
import com.example.grader.entity.Problem
import com.example.grader.error.BadRequestException
import com.example.grader.error.ProblemNotFoundException
import com.example.grader.repository.ProblemRepository
import io.mockk.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.data.repository.findByIdOrNull

class ProblemServiceTest {

    private val problemRepository: ProblemRepository = mockk()
    private val s3Service: AwsS3Service = mockk()
    private lateinit var service: ProblemService

    @BeforeEach
    fun setUp() {
        service = ProblemService(problemRepository, s3Service)
    }

    @Test
    fun `addNewProblem - success`() {
        val request = ProblemRequest("Title", Difficulty.EASY, mockk { every { isEmpty } returns false })
        val pdfKey = "s3key.pdf"
        val savedProblem = Problem(1L, "Title", Difficulty.EASY, pdfKey)

        every { s3Service.savePdfToS3(request.pdf) } returns pdfKey
        every { problemRepository.save(any()) } returns savedProblem

        val result = service.addNewProblem(request)

        assertEquals(savedProblem.id, result.id)
        assertEquals(savedProblem.title, result.title)
        verify { s3Service.savePdfToS3(request.pdf) }
        verify { problemRepository.save(any()) }
    }

    @Test
    fun `addNewProblem - missing title or pdf`() {
        val request = ProblemRequest("", Difficulty.EASY, mockk { every { isEmpty } returns true })

        val ex = assertThrows(BadRequestException::class.java) {
            service.addNewProblem(request)
        }
        assertTrue(ex.message!!.contains("Title and PDF"))
    }

    @Test
    fun `listAllProblems - returns list with presigned urls`() {
        val problems = listOf(
            Problem(1L, "A", Difficulty.EASY, "file1.pdf"),
            Problem(2L, "B", Difficulty.HARD, "file2.pdf")
        )

        every { problemRepository.findAll() } returns problems
        every { s3Service.generatePresignedUrl(any()) } answers { "https://s3.url/${firstArg<String>()}" }

        val result = service.listAllProblems()

        assertEquals(2, result.size)
        assertTrue(result.all { it.pdf.startsWith("https://s3.url/") })
        verify(exactly = 2) { s3Service.generatePresignedUrl(any()) }
    }

    @Test
    fun `getProblemById - found`() {
        val problem = Problem(1L, "A", Difficulty.MEDIUM, "key.pdf")
        every { problemRepository.findByIdOrNull(1L) } returns problem
        every { s3Service.generatePresignedUrl("key.pdf") } returns "https://s3.url/key.pdf"

        val result = service.getProblemById(1L)

        assertEquals(1L, result.id)
        assertEquals("https://s3.url/key.pdf", result.pdf)
    }

    @Test
    fun `getProblemById - not found`() {
        every { problemRepository.findByIdOrNull(1L) } returns null

        assertThrows(ProblemNotFoundException::class.java) {
            service.getProblemById(1L)
        }
    }

    @Test
    fun `updateProblem - success`() {
        val oldProblem = Problem(1L, "Old", Difficulty.EASY, "old.pdf")
        val request = ProblemRequest("New", Difficulty.HARD, mockk { every { isEmpty } returns false })
        val newPdfKey = "new.pdf"

        every { problemRepository.findByIdOrNull(1L) } returns oldProblem
        every { s3Service.savePdfToS3(request.pdf) } returns newPdfKey
        every { problemRepository.save(any()) } answers { firstArg() as Problem }
        every { s3Service.generatePresignedUrl(newPdfKey) } returns "https://s3.url/new.pdf"

        val result = service.updateProblem(1L, request)

        assertEquals("New", result.title)
        assertEquals(Difficulty.HARD, result.difficulty)
        assertEquals("https://s3.url/new.pdf", result.pdf)
    }

    @Test
    fun `updateProblem - not found`() {
        val request = ProblemRequest("New", Difficulty.HARD, mockk { every { isEmpty } returns false })

        every { problemRepository.findByIdOrNull(1L) } returns null

        assertThrows(ProblemNotFoundException::class.java) {
            service.updateProblem(1L, request)
        }
    }

    @Test
    fun `updateProblem - invalid input`() {
        val request = ProblemRequest("", Difficulty.EASY, mockk { every { isEmpty } returns true })

        val ex = assertThrows(BadRequestException::class.java) {
            service.updateProblem(1L, request)
        }
        assertTrue(ex.message!!.contains("Title and PDF"))
    }

    @Test
    fun `deleteProblem - success`() {
        val problem = Problem(1L, "A", Difficulty.EASY, "key.pdf")

        every { problemRepository.findByIdOrNull(1L) } returns problem
        every { problemRepository.delete(problem) } just Runs

        service.deleteProblem(1L)

        verify { problemRepository.delete(problem) }
    }

    @Test
    fun `deleteProblem - not found`() {
        every { problemRepository.findByIdOrNull(1L) } returns null

        assertThrows(ProblemNotFoundException::class.java) {
            service.deleteProblem(1L)
        }
    }
}