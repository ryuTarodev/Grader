package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.SubmissionDto
import com.example.grader.dto.TagDto
import com.example.grader.entity.Tag
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

    fun createTag(tagName: String): ApiResponse<TagDto> {
        return try {
            val existingTag = tagRepository.findByIdOrNull(tagName)
            if (existingTag != null) {
                return ResponseUtil.badRequest(
                    message = "Tag with ID $tagName already exists",
                    data = TagDto(),
                )
            }

            val newTag = Tag(name = tagName)
            tagRepository.save(newTag)

            ResponseUtil.success(
                message = "Tag created successfully",
                data = newTag.toTagDTO(),
                metadata = null
            )
        } catch (ex: Exception) {
            logger.error("Failed to create tag", ex)
            ResponseUtil.internalServerError(
                message = "Failed to create tag",
                data = TagDto(),
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

    fun getTagById(id: String): ApiResponse<TagDto> {
        return try {
            val tag = tagRepository.findByIdOrNull(id)
            if (tag == null) {
                return ResponseUtil.notFound(
                    message = "Tag with ID $id not found",
                    data = TagDto(),
                )
            }
            ResponseUtil.success(
                message = "Tag found",
                data = tag.toTagDTO(),
                metadata = null
            )
        } catch (ex: Exception) {
            logger.error("Failed to get tag by ID", ex)
            ResponseUtil.internalServerError(
                message = "Failed to get tag",
                data = TagDto(),
            )
        }
    }

    fun updateTag(tag: Tag): ApiResponse<TagDto> {
        return try {
            val existingTag = tagRepository.findByIdOrNull(tag.name)
            if (existingTag == null) {
                return ResponseUtil.notFound(
                    message = "Tag with ID ${tag.name} not found",
                    data = TagDto(),
                )
            }

            val updatedTag = tagRepository.save(tag)
            ResponseUtil.success(
                message = "Tag updated successfully",
                data = updatedTag.toTagDTO(),
                metadata = null
            )
        } catch (ex: Exception) {
            logger.error("Failed to update tag", ex)
            ResponseUtil.internalServerError(
                message = "Failed to update tag",
                data = TagDto(),
            )
        }
    }

    fun deleteTag(tag: Tag): ApiResponse<TagDto> {
        return try {
            val existingTag = tagRepository.findByIdOrNull(tag.name)
                ?: return ResponseUtil.notFound(
                    message = "Tag with ID ${tag.name} not found",
                    data = TagDto(),
                )

            tagRepository.delete(existingTag)
            ResponseUtil.success(
                message = "Tag deleted successfully",
                data = existingTag.toTagDTO(),
                metadata = null
            )
        } catch (ex: Exception) {
            logger.error("Failed to delete tag", ex)
            ResponseUtil.internalServerError(
                message = "Failed to delete tag",
                data = TagDto(),
            )
        }
    }
}