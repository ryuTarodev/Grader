package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.entity.Tag
import com.example.grader.entity.Problem
import com.example.grader.entity.ProblemTag
import com.example.grader.error.*
import com.example.grader.repository.*
import com.example.grader.util.*
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProblemTagService(
    private val problemRepository: ProblemRepository,
    private val tagRepository: TagRepository,
    private val problemTagRepository: ProblemTagRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)


    fun createProblemTag(problemId: Long, tagId: Long): ApiResponse<ProblemTag> {
        return try {
            val problem = problemRepository.findByIdOrNull(problemId)
                ?: throw ProblemNotFoundException("Problem not found")

            val tag = tagRepository.findByIdOrNull(tagId)
                ?: throw TagNotFoundException("Tag not found")

            val problemTag = ProblemTag(problem = problem, tag = tag)

            val savedProblemTag = problemTagRepository.save(problemTag)

            ResponseUtil.success(
                message = "ProblemTag created successfully.",
                data = savedProblemTag,
                metadata = null
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred while creating ProblemTag", e)
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = ProblemTag()
            )
        }
    }

    fun getProblemTagsByProblemId(problemId: Long): ApiResponse<List<ProblemTag>> {
        return try {
            val problemTags = problemTagRepository.findByProblemId(problemId)
                ?: throw ProblemTagNotFoundException("ProblemTags not found for problemId $problemId")

            ResponseUtil.success(
                message = "ProblemTags fetched successfully.",
                data = problemTags,
                metadata = null
            )
        } catch (e: ProblemTagNotFoundException) {
            logger.error("ProblemTagNotFound: ${e.message}")
            ResponseUtil.notFound(
                message = "Invalid request: ${e.message}",
                data = emptyList()
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred while getting ProblemTags", e)
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = emptyList()
            )
        }
    }

    fun getProblemTagsByTagId(tagId: Long): ApiResponse<List<ProblemTag>> {
        return try {
            val problemTags = problemTagRepository.findByTagId(tagId)
                ?: throw ProblemTagNotFoundException("ProblemTags not found for tagId $tagId")

            ResponseUtil.success(
                message = "ProblemTags fetched successfully.",
                data = problemTags,
                metadata = null
            )
        } catch (e: ProblemTagNotFoundException) {
            logger.error("ProblemTagNotFound: ${e.message}")
            ResponseUtil.notFound(
                message = "Invalid request: ${e.message}",
                data = emptyList()
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred while getting ProblemTags by tagId", e)
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = emptyList()
            )
        }
    }

    fun updateProblemTag(id: Long, newProblemId: Long, newTagId: Long): ApiResponse<ProblemTag> {
        return try {
            val existingProblemTag = problemTagRepository.findByIdOrNull(id)
                ?: throw ProblemTagNotFoundException("ProblemTag not found")

            val newProblem = problemRepository.findByIdOrNull(newProblemId)
                ?: throw ProblemNotFoundException("Problem not found")

            val newTag = tagRepository.findByIdOrNull(newTagId)
                ?: throw TagNotFoundException("Tag not found")

            existingProblemTag.problem = newProblem
            existingProblemTag.tag = newTag

            val updatedProblemTag = problemTagRepository.save(existingProblemTag)

            ResponseUtil.success(
                message = "ProblemTag updated successfully.",
                data = updatedProblemTag,
                metadata = null
            )
        } catch (e: Exception) {
            logger.error("Error updating ProblemTag", e)
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = ProblemTag()
            )
        }
    }


    fun deleteProblemTag(id: Long): ApiResponse<Unit> {
        return try {
            val existingProblemTag = problemTagRepository.findByIdOrNull(id)
                ?: throw ProblemTagNotFoundException("ProblemTag not found")

            problemTagRepository.delete(existingProblemTag)

            ResponseUtil.success(
                message = "ProblemTag deleted successfully.",
                data = Unit,
                metadata = null
            )
        } catch (e: Exception) {
            logger.error("An unexpected error occurred while deleting ProblemTag", e)
            ResponseUtil.internalServerError(
                message = "An unexpected error occurred: ${e.message}",
                data = Unit
            )
        }
    }
}