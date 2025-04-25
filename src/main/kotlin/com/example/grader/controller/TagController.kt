package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.TagDto
import com.example.grader.service.TagService
import com.example.grader.dto.RequstResponse.TagRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tags")
class TagController(
    private val tagService: TagService
) {

    @PostMapping
    fun createTag(@RequestBody request: TagRequest): ApiResponse<TagDto> {
        return tagService.createTag(request.name)
    }

    @GetMapping
    fun getTags(): ApiResponse<List<TagDto>> {
        return tagService.getTags()
    }

    @GetMapping("/{id}")
    fun getTagById(@PathVariable id: Long): ApiResponse<TagDto> {
        return tagService.getTagById(id)
    }

    @PutMapping("/{id}")
    fun updateTag(
        @PathVariable id: Long,
        @RequestBody request: TagRequest
    ): ApiResponse<TagDto> {
        return tagService.updateTagById(id, request.name)
    }

    @DeleteMapping("/{id}")
    fun deleteTag(@PathVariable id: Long): ApiResponse<TagDto> {
        return tagService.deleteTagById(id)
    }
}