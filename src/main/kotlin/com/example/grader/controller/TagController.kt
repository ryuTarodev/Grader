package com.example.grader.controller

import com.amazonaws.Response
import com.example.grader.dto.ApiResponse
import com.example.grader.dto.TagDto
import com.example.grader.service.TagService
import com.example.grader.dto.RequstResponse.TagRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tags")
class TagController(
    private val tagService: TagService
) {

    @PostMapping
    fun createTag(@RequestBody request: TagRequest): ResponseEntity<ApiResponse<TagDto>> {
        val response =  tagService.createTag(request.name)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getTags(): ResponseEntity<ApiResponse<List<TagDto>>> {
        val response =  tagService.getTags()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getTagById(@PathVariable id: Long): ResponseEntity<ApiResponse<TagDto>> {
        val response = tagService.getTagById(id)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    fun updateTag(
        @PathVariable id: Long,
        @RequestBody request: TagRequest
    ): ResponseEntity<ApiResponse<TagDto>> {
        val response = tagService.updateTagById(id, request.name)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteTag(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        val response = tagService.deleteTagById(id)
        return ResponseEntity.ok(response)
    }
}