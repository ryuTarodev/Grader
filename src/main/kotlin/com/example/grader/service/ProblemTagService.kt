package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.ProblemDto
import com.example.grader.dto.ProblemTagDto

import com.example.grader.entity.ProblemTag
import com.example.grader.error.*
import com.example.grader.repository.*
import com.example.grader.util.*
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import kotlin.math.E

@Service
class ProblemTagService(
    private val problemRepository: ProblemRepository,
    private val tagRepository: TagRepository,
    private val problemTagRepository: ProblemTagRepository
) {
    companion object {
        private val EMPTY_PROBLEMTAG = ProblemTagDto()
    }

    private val logger = LoggerFactory.getLogger(javaClass)


    fun createProblemTag(problemId: Long, tagId: Long): ApiResponse<ProblemTagDto> {

        val problem = problemRepository.findByIdOrNull(problemId)
            ?: throw ProblemNotFoundException("Problem not found")

        val tag = tagRepository.findByIdOrNull(tagId)
            ?: throw TagNotFoundException("Tag not found")

        val problemTag = ProblemTag(problem = problem, tag = tag)

        val savedProblemTag = problemTagRepository.save(problemTag)

        return ResponseUtil.success(
            message = "ProblemTag created successfully.",
            data = savedProblemTag.toProblemTagDTO(),
            metadata = null
        )

    }

    fun getProblemTagsByProblemId(problemId: Long): ApiResponse<List<ProblemTagDto>> {

        val problemTags = problemTagRepository.findByProblemId(problemId)
            ?: throw ProblemTagNotFoundException("ProblemTags not found for problemId $problemId")

        return ResponseUtil.success(
            message = "ProblemTags fetched successfully.",
            data = mapProblemTagListEntityToProblemTagListDTO(problemTags),
            metadata = null
        )

    }

    fun getProblemTagsByTagId(tagId: Long): ApiResponse<List<ProblemTagDto>> {

        val problemTags = problemTagRepository.findByTagId(tagId)
            ?: throw ProblemTagNotFoundException("ProblemTags not found for tagId $tagId")

        return ResponseUtil.success(
            message = "ProblemTags fetched successfully.",
            data = mapProblemTagListEntityToProblemTagListDTO(problemTags),
            metadata = null
        )

    }

    fun updateProblemTag(id: Long, newProblemId: Long, newTagId: Long): ApiResponse<ProblemTagDto> {

        val existingProblemTag = problemTagRepository.findByIdOrNull(id)
            ?: throw ProblemTagNotFoundException("ProblemTag not found")

        val newProblem = problemRepository.findByIdOrNull(newProblemId)
            ?: throw ProblemNotFoundException("Problem not found")

        val newTag = tagRepository.findByIdOrNull(newTagId)
            ?: throw TagNotFoundException("Tag not found")

        existingProblemTag.problem = newProblem
        existingProblemTag.tag = newTag

        val updatedProblemTag = problemTagRepository.save(existingProblemTag)

        return ResponseUtil.success(
            message = "ProblemTag updated successfully.",
            data = updatedProblemTag.toProblemTagDTO(),
            metadata = null
        )

    }


    fun deleteProblemTag(id: Long): ApiResponse<Unit> {

        val existingProblemTag = problemTagRepository.findByIdOrNull(id)
            ?: throw ProblemTagNotFoundException("ProblemTag not found")

        problemTagRepository.delete(existingProblemTag)

        return ResponseUtil.success(
            message = "ProblemTag deleted successfully.",
            data = Unit,
            metadata = null
        )

    }

    fun deleteProblemTagByProblemIdAndTagId(problemId: Long, tagId: Long): ApiResponse<Unit> {

        val problemTag = problemTagRepository.findByProblemIdAndTagId(problemId, tagId)
            ?: throw ProblemTagNotFoundException("ProblemTag not found for problemId=$problemId and tagId=$tagId")

        problemTagRepository.delete(problemTag)

        return ResponseUtil.success(
            message = "Tag removed from Problem successfully.",
            data = Unit,
            metadata = null
        )

    }
}