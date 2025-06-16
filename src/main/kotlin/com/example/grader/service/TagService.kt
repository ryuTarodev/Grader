package com.example.grader.service

import com.example.grader.dto.TagDto
import com.example.grader.entity.Tag
import com.example.grader.error.BadRequestException
import com.example.grader.error.TagNotFoundException
import com.example.grader.repository.TagRepository
import com.example.grader.util.mapTagListEntityToTagListDTO
import com.example.grader.util.toTagDTO
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagService(
    private val tagRepository: TagRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun createTag(tagName: String): TagDto {
        validateTagName(tagName)

        if (tagRepository.existsByName(tagName)) {
            throw BadRequestException("Tag with name [$tagName] already exists")
        }

        val newTag = Tag(name = tagName.trim())
        val savedTag = tagRepository.save(newTag)

        logger.info("Created new tag with ID: ${savedTag.id}")

        return savedTag.toTagDTO()
    }


    fun getTags(): List<TagDto> {
        val tags = tagRepository.findAll()

        return mapTagListEntityToTagListDTO(tags)
    }


    fun getTagById(id: Long): TagDto {
        val tag = findTagById(id)

        return tag.toTagDTO()
    }

    fun updateTagById(id: Long, tagName: String): TagDto {
        validateTagName(tagName)

        val existingTag = findTagById(id)
        val trimmedName = tagName.trim()

        // Check if another tag with the same name exists (excluding current tag)
        if (tagRepository.existsByNameAndIdNot(trimmedName, id)) {
            throw BadRequestException("Tag with name [$trimmedName] already exists")
        }

        existingTag.name = trimmedName
        val updatedTag = tagRepository.save(existingTag)

        logger.info("Updated tag with ID: $id")

        return updatedTag.toTagDTO()
    }

    @Transactional
    fun deleteTagById(id: Long) {
        val tag = findTagById(id)

        tagRepository.delete(tag)

        logger.info("Deleted tag with ID: $id")
    }

    private fun validateTagName(tagName: String) {
        if (tagName.isBlank()) {
            throw BadRequestException("Tag name cannot be blank")
        }

        if (tagName.length > MAX_TAG_NAME_LENGTH) {
            throw BadRequestException("Tag name cannot exceed $MAX_TAG_NAME_LENGTH characters")
        }
    }

    private fun findTagById(id: Long): Tag {
        return tagRepository.findByIdOrNull(id)
            ?: throw TagNotFoundException("Tag with ID $id not found")
    }

    companion object {
        private const val MAX_TAG_NAME_LENGTH = 50 // Adjust as needed
    }
}