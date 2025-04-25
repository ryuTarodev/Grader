package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.SubmissionDto
import com.example.grader.dto.TagDto
import com.example.grader.entity.Tag
import com.example.grader.error.BadRequestException
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
        return try {
            if (tagName.isBlank()) {
                throw BadRequestException("Tag name cannot be blank")
            }

            val existingTag = tagRepository.findByName(tagName)
            if (existingTag != null) {
                throw BadRequestException("Tag with name [$tagName] already exists")
            }

            val newTag = Tag(name = tagName)
            tagRepository.save(newTag)

            ResponseUtil.success(
                message = "Tag created successfully",
                data = newTag.toTagDTO(),
                metadata = null
            )
        } catch (ex: BadRequestException) {
            logger.error("Tag already exists", ex)
            ResponseUtil.badRequest(
                message = "Tag with name $tagName already exists",
                data = EMPTY_TAG,
            )
        } catch (ex: Exception) {
            logger.error("Failed to create tag", ex)
            ResponseUtil.internalServerError(
                message = "Failed to create tag",
                data = EMPTY_TAG,
            )
        }
    }

    fun getTags(): ApiResponse<List<TagDto>> {
        return try {
            val existingTags = tagRepository.findAll()
            ResponseUtil.success(
                message = "Tags retrieved",
                data = mapTagListEntityToTagListDTO(existingTags),
                metadata = null
            )
        } catch (ex: Exception) {
            logger.error("Failed to fetch tags", ex)
            ResponseUtil.internalServerError(
                message = "Failed to fetch tags",
                data = listOf(),
            )
        }
    }

    fun getTagById(id: Long): ApiResponse<TagDto> {
        return try {
            val tag = tagRepository.findByIdOrNull(id)
                ?: return ResponseUtil.notFound(
                    message = "Tag with ID $id not found",
                    data = EMPTY_TAG,
                )
            ResponseUtil.success(
                message = "Tag found",
                data = tag.toTagDTO(),
                metadata = null
            )
        } catch (ex: Exception) {
            logger.error("Failed to get tag by ID", ex)
            ResponseUtil.internalServerError(
                message = "Failed to get tag",
                data = EMPTY_TAG,
            )
        }
    }

    fun updateTagById(id: Long, tagName: String): ApiResponse<TagDto> {
        return try {
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
            ResponseUtil.success(
                message = "Tag updated successfully",
                data = updatedTag.toTagDTO(),
                metadata = null
            )
        } catch (ex: Exception) {
            logger.error("Failed to update tag", ex)
            ResponseUtil.internalServerError(
                message = "Failed to update tag",
                data = EMPTY_TAG,
            )
        }
    }

    fun deleteTagById(id: Long): ApiResponse<TagDto> {
        return try {
            tagRepository.findByIdOrNull(id)?.let { tag ->
                tagRepository.delete(tag)
                return ResponseUtil.success(
                    message = "Tag deleted successfully",
                    data = tag.toTagDTO(),
                    metadata = null
                )
            } ?: return ResponseUtil.notFound(
                message = "Tag with ID $id not found",
                data = EMPTY_TAG,
            )
        } catch (ex: Exception) {
            logger.error("Failed to delete tag", ex)
            ResponseUtil.internalServerError(
                message = "Failed to delete tag",
                data = EMPTY_TAG,
            )
        }
    }
}