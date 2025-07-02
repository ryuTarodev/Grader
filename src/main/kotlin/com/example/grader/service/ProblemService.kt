package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.ProblemDto
import com.example.grader.entity.Difficulty
import com.example.grader.entity.Problem
import com.example.grader.error.BadRequestException
import com.example.grader.error.ProblemNotFoundException
import com.example.grader.repository.ProblemRepository
import com.example.grader.util.ResponseUtil
import com.example.grader.util.mapProblemListEntityToProblemListDTO
import com.example.grader.util.toProblemDTO
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ProblemService(
    private val problemRepository: ProblemRepository,
    private val s3Service: AwsS3Service
) {
    private val logger = LoggerFactory.getLogger(ProblemService::class.java)

    fun addNewProblem(title: String, difficulty: Difficulty, pdf: MultipartFile): ProblemDto {
        logger.debug("addNewProblem called with title: '$title'")

        if (title.isBlank() || pdf.isEmpty) {
            logger.warn("Invalid input: Title or PDF is empty")
            throw BadRequestException("Title and PDF cannot be blank or empty")
        }

        val pdfKey = s3Service.savePdfToS3(pdf)
        val problem = Problem(title = title, difficulty = difficulty, pdf = pdfKey)
        val savedProblem = problemRepository.save(problem)

        return savedProblem.toProblemDTO()
    }

    @Cacheable(value = ["problems"])
    fun listAllProblems(): List<ProblemDto> {
        logger.debug("listAllProblems called")

        val problems = problemRepository.findAll()
        val problemsWithUrls = problems.map { problem ->
            problem.pdf = s3Service.generatePresignedUrl(problem.pdf)
            problem
        }

        return mapProblemListEntityToProblemListDTO(problemsWithUrls)
    }

    @Cacheable(value = ["problem"], key = "#id")
    fun getProblemById(id: Long): ProblemDto {
        logger.debug("getProblemById called with ID: $id")

        val problem = problemRepository.findByIdOrNull(id)
            ?: throw ProblemNotFoundException("Problem not found with ID: $id")

        problem.pdf = s3Service.generatePresignedUrl(problem.pdf)

        return problem.toProblemDTO()
    }

    @CachePut(value = ["problem"], key = "#id")
    fun updateProblem(id: Long, title: String, difficulty: Difficulty, pdf: MultipartFile): ProblemDto {
        logger.debug("updateProblem called with ID: $id")

        if (title.isBlank() || pdf.isEmpty) {
            logger.warn("Invalid input: Title or PDF is empty")
            throw BadRequestException("Title and PDF cannot be blank or empty")
        }

        val problem = problemRepository.findByIdOrNull(id)
            ?: throw ProblemNotFoundException("Problem not found with ID: $id")

        problem.title = title
        problem.difficulty = difficulty

        val newPdfKey = s3Service.savePdfToS3(pdf)
        problem.pdf = newPdfKey

        val updatedProblem = problemRepository.save(problem)
        updatedProblem.pdf = s3Service.generatePresignedUrl(updatedProblem.pdf)

        return updatedProblem.toProblemDTO()
    }

    @CacheEvict(value = ["problem"], key = "#id")
    fun deleteProblem(id: Long) {
        logger.debug("deleteProblem called with ID: $id")

        val problem = problemRepository.findByIdOrNull(id)
            ?: throw ProblemNotFoundException("Problem not found with ID: $id")

        problemRepository.delete(problem)

        logger.info("Problem deleted successfully with ID: $id")
    }
}