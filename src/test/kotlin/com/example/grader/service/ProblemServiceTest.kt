package com.example.grader.service

import com.example.grader.dto.RequstResponse.ProblemRequest
import com.example.grader.entity.Difficulty
import com.example.grader.entity.Problem
import com.example.grader.error.ProblemNotFoundException
import com.example.grader.repository.ProblemRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.web.multipart.MultipartFile
import java.util.Optional
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class ProblemServiceTest {

    @Mock
    lateinit var problemRepository: ProblemRepository

    @Mock
    lateinit var s3Service: AwsS3Service

    private lateinit var problemService: ProblemService

    @BeforeEach
    fun setup() {
        problemService = ProblemService(problemRepository, s3Service)
    }

    @Test
    fun `addNewProblem should save and return created problem`() {
        val mockPdf = mock(MultipartFile::class.java)
        val request = ProblemRequest("New Problem", Difficulty.MEDIUM, mockPdf)

        `when`(s3Service.savePdfToS3(mockPdf)).thenReturn("uploaded-key.pdf")

        val savedProblem = Problem(1L, "New Problem", Difficulty.MEDIUM, "uploaded-key.pdf")
        `when`(problemRepository.save(any())).thenReturn(savedProblem)

        val response = problemService.addNewProblem(request)

        assertEquals("Problem created successfully", response.message)
        assertEquals("New Problem", response.data?.title)
        assertEquals("uploaded-key.pdf", response.data?.pdf)
    }

    @Test
    fun `listAllProblems should return list with presigned URLs`() {
        val problems = listOf(
            Problem(1L, "P1", Difficulty.EASY, "p1.pdf"),
            Problem(2L, "P2", Difficulty.HARD, "p2.pdf")
        )

        `when`(problemRepository.findAll()).thenReturn(problems)
        `when`(s3Service.generatePresignedUrl("p1.pdf")).thenReturn("url1")
        `when`(s3Service.generatePresignedUrl("p2.pdf")).thenReturn("url2")

        val response = problemService.listAllProblems()

        assertEquals(2, response.data?.size)
        assertEquals("List of Problems retrieved successfully", response.message)
    }

    @Test
    fun `getProblemById should return problem with presigned URL`() {
        val problem = Problem(1L, "Test Problem", Difficulty.EASY, "test.pdf")

        `when`(problemRepository.findById(any())).thenReturn(Optional.of(problem))
        `when`(s3Service.generatePresignedUrl("test.pdf")).thenReturn("presigned-url")

        val response = problemService.getProblemById(1L)

        assertEquals("Problem retrieved successfully", response.message)
        assertEquals("Test Problem", response.data?.title)
        assertEquals("presigned-url", response.data?.pdf)
    }

    @Test
    fun `getProblemById should throw when not found`() {
        `when`(problemRepository.findById(any())).thenReturn(Optional.empty())

        assertFailsWith<ProblemNotFoundException> {
            problemService.getProblemById(999L)
        }
    }

    @Test
    fun `updateProblem should update fields and return updated problem`() {
        val mockPdf: MultipartFile = mock()
        val request = ProblemRequest("Updated Title", Difficulty.HARD, mockPdf)

        val existing = Problem(1L, "Old Title", Difficulty.EASY, "old.pdf")
        `when`(mockPdf.isEmpty).thenReturn(false)
        `when`(s3Service.savePdfToS3(mockPdf)).thenReturn("uploaded-key.pdf")
        `when`(problemRepository.findById(any())).thenReturn(Optional.of(existing))

        val updated = Problem(1L, "Updated Title", Difficulty.HARD, "uploaded-key.pdf")
        `when`(problemRepository.save(any())).thenReturn(updated)
        `when`(s3Service.generatePresignedUrl("uploaded-key.pdf")).thenReturn("presigned-url")

        val response = problemService.updateProblem(1L, request)

        assertEquals("Problem updated successfully", response.message)
        assertEquals("Updated Title", response.data?.title)
        assertEquals("presigned-url", response.data?.pdf)
    }

    @Test
    fun `updateProblem should return not found when missing`() {
        val mockPdf: MultipartFile = mock()
        val request = ProblemRequest("Any", Difficulty.EASY, mockPdf)

        `when`(problemRepository.findById(any())).thenReturn(Optional.empty())

        val response = problemService.updateProblem(999L, request)

        assertEquals("Problem not found", response.message)
    }

    @Test
    fun `deleteProblem should delete if found`() {
        val problem = Problem(1L, "Delete Me", Difficulty.MEDIUM, "delete.pdf")

        `when`(problemRepository.findById(any())).thenReturn(Optional.of(problem))

        val response = problemService.deleteProblem(1L)

        assertEquals("Problem deleted successfully", response.message)
    }

    @Test
    fun `deleteProblem should return not found if problem missing`() {
        `when`(problemRepository.findById(any())).thenReturn(Optional.empty())

        val response = problemService.deleteProblem(999L)

        assertEquals("Problem not found", response.message)
    }
}
