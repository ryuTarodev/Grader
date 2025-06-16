package com.example.grader.service

import com.example.grader.dto.ProblemDto
import com.example.grader.dto.RequesttResponse.ProblemRequest
import com.example.grader.entity.Problem
import com.example.grader.error.BadRequestException
import com.example.grader.error.ProblemNotFoundException
import com.example.grader.repository.ProblemRepository
import com.example.grader.util.mapProblemListEntityToProblemListDTO
import com.example.grader.util.toProblemDTO
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ProblemService(
    private val problemRepository: ProblemRepository,
    private val s3Service: AwsS3Service
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun addNewProblem(problemRequest: ProblemRequest): ProblemDto {
        // Validate input
        if (problemRequest.title.isBlank() || problemRequest.pdf.isEmpty) {
            throw BadRequestException("Title and PDF cannot be blank or empty")
        }

        logger.info("Creating new problem with title: ${problemRequest.title}")

        // Upload PDF to S3
        val pdfKey = s3Service.savePdfToS3(problemRequest.pdf)

        // Create and save problem
        val problem = Problem(
            title = problemRequest.title,
            difficulty = problemRequest.difficulty,
            pdf = pdfKey
        )

        val savedProblem = problemRepository.save(problem)
        logger.info("Problem created successfully with ID: ${savedProblem.id}")

        return savedProblem.toProblemDTO()
    }

    fun listAllProblems(): List<ProblemDto> {
        logger.info("Retrieving all problems")

        val problems = problemRepository.findAll()

        // Generate presigned URLs for each problem
        val problemsWithUrls = problems.map { problem ->
            problem.pdf = s3Service.generatePresignedUrl(problem.pdf)
            problem
        }

        return mapProblemListEntityToProblemListDTO(problemsWithUrls)
    }

    fun getProblemById(id: Long): ProblemDto {
        logger.info("Retrieving problem with ID: $id")

        val problem = problemRepository.findByIdOrNull(id)
            ?: throw ProblemNotFoundException("Problem not found with ID: $id")

        // Generate presigned URL for PDF
        problem.pdf = s3Service.generatePresignedUrl(problem.pdf)

        return problem.toProblemDTO()
    }

    fun updateProblem(id: Long, problemRequest: ProblemRequest): ProblemDto {
        // Validate input
        if (problemRequest.title.isBlank() || problemRequest.pdf.isEmpty) {
            throw BadRequestException("Title and PDF cannot be blank or empty")
        }

        logger.info("Updating problem with ID: $id")

        // Find existing problem
        val problem = problemRepository.findByIdOrNull(id)
            ?: throw ProblemNotFoundException("Problem not found with ID: $id")

        // Update fields
        problem.title = problemRequest.title
        problem.difficulty = problemRequest.difficulty

        // Upload new PDF if provided
        val newPdfKey = s3Service.savePdfToS3(problemRequest.pdf)
        problem.pdf = newPdfKey

        // Save and return
        val updatedProblem = problemRepository.save(problem)
        updatedProblem.pdf = s3Service.generatePresignedUrl(updatedProblem.pdf)

        logger.info("Problem updated successfully with ID: $id")
        return updatedProblem.toProblemDTO()
    }

    fun deleteProblem(id: Long) {
        logger.info("Deleting problem with ID: $id")

        val problem = problemRepository.findByIdOrNull(id)
            ?: throw ProblemNotFoundException("Problem not found with ID: $id")

        // Delete from S3 if needed (optional - you might want to keep files for audit)
        // s3Service.deleteFile(problem.pdf)

        problemRepository.delete(problem)
        logger.info("Problem deleted successfully with ID: $id")
    }
}