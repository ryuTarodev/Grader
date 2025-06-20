package com.example.grader.service

import com.example.grader.dto.ProblemDto
import com.example.grader.dto.RequestResponse.ProblemRequest
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
    private val logger = LoggerFactory.getLogger(ProblemService::class.java)

    fun addNewProblem(problemRequest: ProblemRequest): ProblemDto {
        logger.debug("addNewProblem called with title: '${problemRequest.title}'")

        if (problemRequest.title.isBlank() || problemRequest.pdf.isEmpty) {
            logger.warn("Invalid ProblemRequest: Title or PDF is empty")
            throw BadRequestException("Title and PDF cannot be blank or empty")
        }

        logger.info("Uploading PDF to S3 for new problem: '${problemRequest.title}'")
        val pdfKey = s3Service.savePdfToS3(problemRequest.pdf)
        logger.debug("PDF uploaded with S3 key: $pdfKey")

        val problem = Problem(
            title = problemRequest.title,
            difficulty = problemRequest.difficulty,
            pdf = pdfKey
        )

        val savedProblem = problemRepository.save(problem)
        logger.info("Problem successfully saved with ID: ${savedProblem.id}")

        return savedProblem.toProblemDTO()
    }

    fun listAllProblems(): List<ProblemDto> {
        logger.debug("listAllProblems called")

        val problems = problemRepository.findAll()
        logger.info("Found ${problems.size} problems in repository")

        val problemsWithUrls = problems.map { problem ->
            logger.debug("Generating presigned URL for problem ID: ${problem.id}")
            problem.pdf = s3Service.generatePresignedUrl(problem.pdf)
            problem
        }

        return mapProblemListEntityToProblemListDTO(problemsWithUrls)
    }

    fun getProblemById(id: Long): ProblemDto {
        logger.debug("getProblemById called with ID: $id")

        val problem = problemRepository.findByIdOrNull(id)
        if (problem == null) {
            logger.warn("Problem not found with ID: $id")
            throw ProblemNotFoundException("Problem not found with ID: $id")
        }

        logger.info("Problem found with ID: $id, generating presigned URL")
        problem.pdf = s3Service.generatePresignedUrl(problem.pdf)

        return problem.toProblemDTO()
    }

    fun updateProblem(id: Long, problemRequest: ProblemRequest): ProblemDto {
        logger.debug("updateProblem called with ID: $id")

        if (problemRequest.title.isBlank() || problemRequest.pdf.isEmpty) {
            logger.warn("Invalid ProblemRequest: Title or PDF is empty")
            throw BadRequestException("Title and PDF cannot be blank or empty")
        }

        val problem = problemRepository.findByIdOrNull(id)
        if (problem == null) {
            logger.warn("Problem not found with ID: $id")
            throw ProblemNotFoundException("Problem not found with ID: $id")
        }

        logger.info("Updating problem with ID: $id")
        problem.title = problemRequest.title
        problem.difficulty = problemRequest.difficulty

        logger.info("Uploading new PDF to S3 for problem ID: $id")
        val newPdfKey = s3Service.savePdfToS3(problemRequest.pdf)
        logger.debug("New PDF uploaded with S3 key: $newPdfKey")

        problem.pdf = newPdfKey
        val updatedProblem = problemRepository.save(problem)
        logger.info("Problem updated successfully with ID: $id")

        updatedProblem.pdf = s3Service.generatePresignedUrl(updatedProblem.pdf)
        return updatedProblem.toProblemDTO()
    }

    fun deleteProblem(id: Long) {
        logger.debug("deleteProblem called with ID: $id")

        val problem = problemRepository.findByIdOrNull(id)
        if (problem == null) {
            logger.warn("Problem not found with ID: $id")
            throw ProblemNotFoundException("Problem not found with ID: $id")
        }

        logger.info("Deleting problem with ID: $id from database")
        problemRepository.delete(problem)

        // Optional S3 cleanup
        // logger.info("Deleting PDF from S3 with key: ${problem.pdf}")
        // s3Service.deleteFile(problem.pdf)

        logger.info("Problem deleted successfully with ID: $id")
    }
}