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
            val problemDto = savedProblem.toProblemDTO()

            ResponseUtil.created(
                message = "Problem created successfully",
                data = problemDto,
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

            val problemListDto = mapProblemListEntityToProblemListDTO(savedProblems)

            ResponseUtil.success(
                message = "List of Problems retrieved successfully",
                data = problemListDto,
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
    fun updateProblem(
        id: Long,
        title: String,
        difficulty: Difficulty,
        pdf: MultipartFile?
    ): ApiResponse<ProblemDto> {
        return try {
            val problem = problemRepository.findByIdOrNull(id)
                ?: return ResponseUtil.notFound("Problem not found", ProblemDto())

            problem.title = title
            problem.difficulty = difficulty

            if (pdf != null && !pdf.isEmpty) {
                val newPdfKey = s3Service.savePdfToS3(pdf)
                problem.pdf = newPdfKey
            }

            val updatedProblem = problemRepository.save(problem)
            updatedProblem.pdf = s3Service.generatePresignedUrl(updatedProblem.pdf)

            val updatedDto = updatedProblem.toProblemDTO()

            ResponseUtil.success(
                message = "Problem updated successfully",
                data = updatedDto,
                metadata = null
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred while updating problem", e)
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = ProblemDto()
            )
        }
    }


    fun deleteProblem(id: Long): ApiResponse<Unit> {
        return try {
            val problem = problemRepository.findByIdOrNull(id)
                ?: return ResponseUtil.notFound(
                    message = "Problem not found",
                    data = Unit
                )

            problemRepository.delete(problem)

            ResponseUtil.success(
                message = "Problem deleted successfully",
                data = Unit,
                metadata = null
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred while deleting problem", e)
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = Unit
            )
        }
    }
}