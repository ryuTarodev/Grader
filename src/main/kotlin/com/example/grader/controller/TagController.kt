package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.RequestResponse.TagRequest
import com.example.grader.dto.TagDto
import com.example.grader.service.TagService
import com.example.grader.util.ResponseUtil
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tags")
class TagController(
    private val tagService: TagService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Create a new tag
    @PostMapping
    fun createTag(@Valid @RequestBody request: TagRequest): ResponseEntity<ApiResponse<TagDto>> {
        logger.info("Received request to create tag with name: ${request.name}")

        val tag = tagService.createTag(request.name)
        val response = ResponseUtil.created("Tag created successfully", tag, null)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // Get all tags
    @GetMapping
    fun getAllTags(): ResponseEntity<ApiResponse<List<TagDto>>> {
        logger.info("Received request to get all tags")

        val tags = tagService.getTags()
        val response = ResponseUtil.success("Tags retrieved", tags, null)
        return ResponseEntity.ok(response)
    }

    // Get a specific tag by ID
    @GetMapping("/{id}")
    fun getTagById(@PathVariable id: Long): ResponseEntity<ApiResponse<TagDto>> {
        logger.info("Received request to get tag with ID: $id")

        val tag = tagService.getTagById(id)
        val response = ResponseUtil.success("Tag retrieved successfully", tag, null)
        return ResponseEntity.ok(response)
    }

    // Update a tag
    @PutMapping("/{id}")
    fun updateTag(
        @PathVariable id: Long,
        @Valid @RequestBody request: TagRequest
    ): ResponseEntity<ApiResponse<TagDto>> {
        logger.info("Received request to update tag with ID: $id, new name: ${request.name}")

        val updatedTag = tagService.updateTagById(id, request.name)
        val response = ResponseUtil.success("Tag updated successfully", updatedTag, null)
        return ResponseEntity.ok(response)
    }

    // Delete a tag
    @DeleteMapping("/{id}")
    fun deleteTag(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        logger.info("Received request to delete tag with ID: $id")

        tagService.deleteTagById(id)
        val response = ResponseUtil.success("Tag deleted successfully", Unit, null)
        return ResponseEntity.ok(response)
    }
}