package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.SubmissionDto
import com.example.grader.dto.TagDto
import com.example.grader.entity.Tag
import com.example.grader.error.BadRequestException
import com.example.grader.error.TagNotFoundException
import com.example.grader.repository.TagRepository
import com.example.grader.util.ResponseUtil
import com.example.grader.util.mapTagListEntityToTagListDTO
import com.example.grader.util.toTagDTO
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class TagService(
    private val tagRepository: TagRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private val EMPTY_TAG = TagDto()
    }

    fun createTag(tagName: String): ApiResponse<TagDto> {

        if (tagName.isBlank()) {
            throw BadRequestException("Tag name cannot be blank")
        }

        val existingTag = tagRepository.findByName(tagName)
        if (existingTag != null) {
            throw BadRequestException("Tag with name [$tagName] already exists")
        }

        val newTag = Tag(name = tagName)
        tagRepository.save(newTag)

        return ResponseUtil.success(
            message = "Tag created successfully",
            data = newTag.toTagDTO(),
            metadata = null
        )

    }

    fun getTags(): ApiResponse<List<TagDto>> {

        val existingTags = tagRepository.findAll()
        return ResponseUtil.success(
            message = "Tags retrieved",
            data = mapTagListEntityToTagListDTO(existingTags),
            metadata = null
        )

    }

    fun getTagById(id: Long): ApiResponse<TagDto> {

        val tag = tagRepository.findByIdOrNull(id)
            ?: return ResponseUtil.notFound(
                message = "Tag with ID $id not found",
                data = EMPTY_TAG,
            )
        return ResponseUtil.success(
            message = "Tag found",
            data = tag.toTagDTO(),
            metadata = null
        )

    }

    fun updateTagById(id: Long, tagName: String): ApiResponse<TagDto> {

        if (tagName.isBlank()) {
            return ResponseUtil.badRequest(
                message = "Tag name cannot be blank",
                data = EMPTY_TAG,
            )
        }

        val existingTag = tagRepository.findByIdOrNull(id)
            ?: return ResponseUtil.notFound(
                message = "Tag with ID $id not found",
                data = EMPTY_TAG,
            )

        existingTag.name = tagName
        val updatedTag = tagRepository.save(existingTag)
        return ResponseUtil.success(
            message = "Tag updated successfully",
            data = updatedTag.toTagDTO(),
            metadata = null
        )

    }

    fun deleteTagById(id: Long): ApiResponse<Unit> {
        val tag = tagRepository.findByIdOrNull(id)
            ?: throw TagNotFoundException("Tag with id $id not found")

        tagRepository.delete(tag)

        return ResponseUtil.success(
            message = "Tag deleted successfully",
            data = Unit,
            metadata = null
        )
    }
}