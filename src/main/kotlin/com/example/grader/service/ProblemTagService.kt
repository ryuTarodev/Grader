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
        logger.info("Attempting to create ProblemTag (problemId=$problemId, tagId=$tagId)")

        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("Problem not found with ID: $problemId")


        val tag = tagRepository.findByIdOrNull(tagId)
            ?: throw TagNotFoundException("Tag not found with ID: $tagId")


        if (problemTagRepository.findByProblemIdAndTagId(problemId, tagId) != null) {
            logger.warn("Duplicate ProblemTag (problemId=$problemId, tagId=$tagId)")
            throw DuplicateException("ProblemTag already exists")
        }

        val savedProblemTag = problemTagRepository.save(ProblemTag(problem = problem, tag = tag))
        logger.info("ProblemTag created successfully: ID=${savedProblemTag.id}")
        return savedProblemTag.toProblemTagDTO()
    }

    fun getAllProblemTags(): List<ProblemTagDto> {
        logger.info("Fetching all ProblemTags")
        return mapProblemTagListEntityToProblemTagListDTO(problemTagRepository.findAll())
    }

    fun getProblemTagsByProblemId(problemId: Long): List<ProblemTagDto> {
        logger.info("Fetching ProblemTags for problemId=$problemId")

        problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("Problem not found with ID: $problemId")


        val tags = problemTagRepository.findByProblemId(problemId)
            ?: throw ProblemNotFoundException("Tag not found with ID: $problemId")


        return mapProblemTagListEntityToProblemTagListDTO(tags)
    }

    fun getProblemTagsByTagId(tagId: Long): List<ProblemTagDto> {
        logger.info("Fetching ProblemTags for tagId=$tagId")

        tagRepository.findByIdOrNull(tagId)
            ?: run {
                logger.warn("Tag not found with ID: $tagId")
                throw TagNotFoundException("Tag not found with ID: $tagId")
            }

        val problemTags = problemTagRepository.findByTagId(tagId)
            ?: run {
                logger.warn("No ProblemTags found for tagId: $tagId")
                throw TagNotFoundException("Tag not found with ID: $tagId")
            }

        return mapProblemTagListEntityToProblemTagListDTO(problemTags)
    }

    fun updateProblemTag(id: Long, newProblemId: Long, newTagId: Long): ProblemTagDto {
        logger.info("Updating ProblemTag ID=$id to problemId=$newProblemId and tagId=$newTagId")

        val existing = problemTagRepository.findByIdOrNull(id)
            ?: throw ProblemTagNotFoundException("ProblemTag not found with ID: $id")


        val newProblem = problemRepository.findByIdOrNull(newProblemId)
            ?: throw ProblemNotFoundException("Problem not found with ID: $newProblemId")


        val newTag = tagRepository.findByIdOrNull(newTagId)
            ?: throw TagNotFoundException("Tag not found with ID: $newTagId")


        val duplicate = problemTagRepository.findByProblemIdAndTagId(newProblemId, newTagId)
        if (duplicate != null && duplicate.id != id) {
            logger.warn("Update would result in duplicate ProblemTag (problemId=$newProblemId, tagId=$newTagId)")
            throw DuplicateException("Duplicate ProblemTag exists")
        }

        existing.problem = newProblem
        existing.tag = newTag

        val updated = problemTagRepository.save(existing)
        logger.info("ProblemTag ID=$id updated successfully")
        return updated.toProblemTagDTO()
    }

    fun deleteProblemTag(id: Long) {
        logger.info("Deleting ProblemTag with ID: $id")

        val existing = problemTagRepository.findByIdOrNull(id)
            ?: run {
                logger.warn("ProblemTag not found with ID: $id")
                throw ProblemTagNotFoundException("ProblemTag not found with ID: $id")
            }

        problemTagRepository.delete(existing)
        logger.info("ProblemTag ID=$id deleted successfully")
    }

    fun deleteProblemTagByProblemIdAndTagId(problemId: Long, tagId: Long) {
        logger.info("Deleting ProblemTag (problemId=$problemId, tagId=$tagId)")

        val tag = problemTagRepository.findByProblemIdAndTagId(problemId, tagId)
            ?: run {
                logger.warn("ProblemTag not found for problemId=$problemId and tagId=$tagId")
                throw ProblemTagNotFoundException("ProblemTag not found")
            }

        problemTagRepository.delete(tag)
        logger.info("ProblemTag (problemId=$problemId, tagId=$tagId) deleted successfully")
    }

    fun addTagToProblem(problemId: Long, tagId: Long): ProblemTagDto {
        logger.debug("Alias method: addTagToProblem(problemId=$problemId, tagId=$tagId)")
        return createProblemTag(problemId, tagId)
    }

    fun removeTagFromProblem(problemId: Long, tagId: Long) {
        logger.debug("Alias method: removeTagFromProblem(problemId=$problemId, tagId=$tagId)")
        deleteProblemTagByProblemIdAndTagId(problemId, tagId)
    }
}