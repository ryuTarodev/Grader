package com.example.grader.service

import com.example.grader.dto.ProblemTagDto
import com.example.grader.entity.ProblemTag
import com.example.grader.error.*
import com.example.grader.repository.*
import com.example.grader.util.*
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ProblemTagService(
    private val problemRepository: ProblemRepository,
    private val tagRepository: TagRepository,
    private val problemTagRepository: ProblemTagRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun createProblemTag(problemId: Long, tagId: Long): ProblemTagDto {
        logger.info("Creating ProblemTag for problemId: $problemId and tagId: $tagId")

        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("Problem not found with ID: $problemId")

        val tag = tagRepository.findByIdOrNull(tagId)
            ?: throw TagNotFoundException("Tag not found with ID: $tagId")

        val existingProblemTag = problemTagRepository.findByProblemIdAndTagId(problemId, tagId)
        if (existingProblemTag != null) {
            throw DuplicateException("ProblemTag already exists for problemId: $problemId and tagId: $tagId")
        }

        val problemTag = ProblemTag(problem = problem, tag = tag)
        val savedProblemTag = problemTagRepository.save(problemTag)

        return savedProblemTag.toProblemTagDTO()
    }

    fun getAllProblemTags(): List<ProblemTagDto> {
        logger.info("Retrieving all ProblemTags")

        val problemTags = problemTagRepository.findAll()
        return mapProblemTagListEntityToProblemTagListDTO(problemTags)
    }


    fun getProblemTagsByProblemId(problemId: Long): List<ProblemTagDto> {
        logger.info("Retrieving ProblemTags for problemId: $problemId")

        // Validate that problem exists
        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("Problem not found with ID: $problemId")

        val problemTags = problemTagRepository.findByProblemId(problemId)?: throw ProblemNotFoundException("Tag not found with ID: $problemId")
        return mapProblemTagListEntityToProblemTagListDTO(problemTags)
    }

    fun getProblemTagsByTagId(tagId: Long): List<ProblemTagDto> {
        logger.info("Retrieving ProblemTags for tagId: $tagId")

        // Validate that tag exists
        val tag = tagRepository.findByIdOrNull(tagId)
            ?: throw TagNotFoundException("Tag not found with ID: $tagId")

        val problemTags = problemTagRepository.findByTagId(tagId) ?: throw TagNotFoundException("Tag not found with ID: $tagId")
        return mapProblemTagListEntityToProblemTagListDTO(problemTags)
    }

    fun updateProblemTag(id: Long, newProblemId: Long, newTagId: Long): ProblemTagDto {
        logger.info("Updating ProblemTag with ID: $id")

        // Find existing ProblemTag
        val existingProblemTag = problemTagRepository.findByIdOrNull(id)
            ?: throw ProblemTagNotFoundException("ProblemTag not found with ID: $id")

        // Validate new problem exists
        val newProblem = problemRepository.findByIdOrNull(newProblemId)
            ?: throw ProblemNotFoundException("Problem not found with ID: $newProblemId")

        // Validate new tag exists
        val newTag = tagRepository.findByIdOrNull(newTagId)
            ?: throw TagNotFoundException("Tag not found with ID: $newTagId")

        // Check if the new combination would create a duplicate (excluding current record)
        val existingWithSameCombo = problemTagRepository.findByProblemIdAndTagId(newProblemId, newTagId)
        if (existingWithSameCombo != null && existingWithSameCombo.id != id) {
            throw DuplicateException("ProblemTag already exists for problemId: $newProblemId and tagId: $newTagId")
        }

        // Update the ProblemTag
        existingProblemTag.problem = newProblem
        existingProblemTag.tag = newTag

        val updatedProblemTag = problemTagRepository.save(existingProblemTag)
        logger.info("ProblemTag updated successfully with ID: $id")

        return updatedProblemTag.toProblemTagDTO()
    }

    fun deleteProblemTag(id: Long) {
        logger.info("Deleting ProblemTag with ID: $id")

        val existingProblemTag = problemTagRepository.findByIdOrNull(id)
            ?: throw ProblemTagNotFoundException("ProblemTag not found with ID: $id")

        problemTagRepository.delete(existingProblemTag)
        logger.info("ProblemTag deleted successfully with ID: $id")
    }

    fun deleteProblemTagByProblemIdAndTagId(problemId: Long, tagId: Long) {
        logger.info("Deleting ProblemTag for problemId: $problemId and tagId: $tagId")

        val problemTag = problemTagRepository.findByProblemIdAndTagId(problemId, tagId)
            ?: throw ProblemTagNotFoundException("ProblemTag not found for problemId: $problemId and tagId: $tagId")

        problemTagRepository.delete(problemTag)
        logger.info("ProblemTag deleted successfully for problemId: $problemId and tagId: $tagId")
    }

    fun addTagToProblem(problemId: Long, tagId: Long): ProblemTagDto {
        // This is an alias for createProblemTag with more semantic naming
        return createProblemTag(problemId, tagId)
    }

    fun removeTagFromProblem(problemId: Long, tagId: Long) {
        // This is an alias for deleteProblemTagByProblemIdAndTagId with more semantic naming
        deleteProblemTagByProblemIdAndTagId(problemId, tagId)
    }
}