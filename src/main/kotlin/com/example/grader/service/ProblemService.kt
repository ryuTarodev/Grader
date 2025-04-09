package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.ProblemDto
import com.example.grader.entity.Difficulty
import com.example.grader.entity.Problem
import com.example.grader.error.ProblemNotFoundException
import com.example.grader.repository.ProblemRepository
import com.example.grader.util.ResponseUtil
import com.example.grader.util.mapProblemListEntityToProblemListDTO
import com.example.grader.util.toProblemDTO
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Instant


@Service
class ProblemService(
    private val problemRepository: ProblemRepository,
    private val s3Service: AwsS3Service
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun addNewProblem(title: String, difficulty: Difficulty, pdf: MultipartFile): ApiResponse<ProblemDto> {
        return try {
            val pdfKey = s3Service.savePdfToS3(pdf)

            val problem = Problem(
                title = title,
                difficulty = difficulty,
                pdf = pdfKey
            )

            val savedProblem = problemRepository.save(problem)
            val problemDTO = savedProblem.toProblemDTO()

            ResponseUtil.created(
                message = "Problem created successfully",
                data = problemDTO,
                metadata = null
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = ProblemDto()
            )
        }
    }

    fun listAllProblems(): ApiResponse<List<ProblemDto>> {
        return try {
            val savedProblems = problemRepository.findAll()
            savedProblems.forEach {
                it.pdf = s3Service.generatePresignedUrl(it.pdf)
            }

            val problemListDTOs = mapProblemListEntityToProblemListDTO(savedProblems)

            ResponseUtil.success(
                message = "List of Problems retrieved successfully",
                data = problemListDTOs,
                metadata = null
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = emptyList()
            )
        }
    }

    fun getProblemById(id: Long): ApiResponse<ProblemDto> {
        return try {
            val savedProblem = problemRepository.findByIdOrNull(id)
                ?: throw ProblemNotFoundException("Problem not found")

            savedProblem.pdf = s3Service.generatePresignedUrl(savedProblem.pdf)

            val problemDTO = savedProblem.toProblemDTO()
            ResponseUtil.success(
                message = "Problem retrieved successfully",
                data = problemDTO,
                metadata = null
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = ProblemDto()
            )
        }
    }

    // Update an existing problem, including uploading a new PDF if provided
    fun updateProblem(id: Long, title: String, difficulty: Difficulty, pdf: MultipartFile?): ApiResponse<ProblemDto> {
        return try {
            val existingProblem = problemRepository.findByIdOrNull(id)
                ?: throw ProblemNotFoundException("Problem not found")

            existingProblem.title = title
            existingProblem.difficulty = difficulty
            existingProblem.updatedAt = Instant.now()

            if (pdf != null && !pdf.isEmpty) {
                val pdfKey = s3Service.savePdfToS3(pdf)
                existingProblem.pdf = pdfKey
            }

            val savedProblem = problemRepository.save(existingProblem)
            val problemDTO = savedProblem.toProblemDTO()

            ResponseUtil.success(
                message = "Problem updated successfully",
                data = problemDTO,
                metadata = null
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred: ${e.message}")
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = ProblemDto()
            )
        }
    }

    // Delete a problem by ID
    fun deleteProblem(id: Long): ApiResponse<ProblemDto?> {
        return try {
            val problem = problemRepository.findByIdOrNull(id)
                ?: return ResponseUtil.notFound(
                    message = "Problem not found",
                    data = null
                )

            val problemDTO = problem.toProblemDTO()
            problemRepository.deleteById(id)

            ResponseUtil.success(
                message = "Problem deleted successfully",
                data = problemDTO,
                metadata = null
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred while deleting problem", e)
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = null
            )
        }
    }
}