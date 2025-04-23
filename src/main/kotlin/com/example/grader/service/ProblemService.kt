package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.ProblemDto
import com.example.grader.dto.RequstResponse.ProblemRequest
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

    fun addNewProblem(problemRequest: ProblemRequest): ApiResponse<ProblemDto> {
        return try {
            val pdfKey = s3Service.savePdfToS3(problemRequest.pdf)

            val problem = Problem(
                title = problemRequest.title,
                difficulty = problemRequest.difficulty,
                pdf = pdfKey
            )

            val savedProblem = problemRepository.save(problem)

            ResponseUtil.created(
                message = "Problem created successfully",
                data = savedProblem.toProblemDTO(),
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



            ResponseUtil.success(
                message = "List of Problems retrieved successfully",
                data = mapProblemListEntityToProblemListDTO(savedProblems),
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

            ResponseUtil.success(
                message = "Problem retrieved successfully",
                data = savedProblem.toProblemDTO(),
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
        problemRequest: ProblemRequest
    ): ApiResponse<ProblemDto> {
        return try {
            val problem = problemRepository.findByIdOrNull(id)
                ?: return ResponseUtil.notFound("Problem not found", ProblemDto())

            problem.title = problemRequest.title
            problem.difficulty = problemRequest.difficulty

            if (!problemRequest.pdf.isEmpty) {
                val newPdfKey = s3Service.savePdfToS3(problemRequest.pdf)
                problem.pdf = newPdfKey
            }

            val updatedProblem = problemRepository.save(problem)
            updatedProblem.pdf = s3Service.generatePresignedUrl(updatedProblem.pdf)



            ResponseUtil.success(
                message = "Problem updated successfully",
                data = updatedProblem.toProblemDTO(),
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