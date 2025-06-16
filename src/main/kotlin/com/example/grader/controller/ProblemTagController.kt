package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.ProblemTagDto
import com.example.grader.service.ProblemTagService
import com.example.grader.util.ResponseUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/problem-tags")
class ProblemTagController(
    private val problemTagService: ProblemTagService
) {

    @PostMapping
    fun createProblemTag(
        @RequestParam problemId: Long,
        @RequestParam tagId: Long
    ): ResponseEntity<ApiResponse<ProblemTagDto>> {
        val problemTagDto = problemTagService.createProblemTag(problemId, tagId)
        val response = ResponseUtil.created("ProblemTag created successfully", problemTagDto, null)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getAllProblemTags(): ResponseEntity<ApiResponse<List<ProblemTagDto>>> {
        val problemTags = problemTagService.getAllProblemTags()
        val response = ResponseUtil.success("ProblemTags retrieved successfully", problemTags, null)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/problem/{problemId}")
    fun getProblemTagsByProblemId(@PathVariable problemId: Long): ResponseEntity<ApiResponse<List<ProblemTagDto>>> {
        val problemTags = problemTagService.getProblemTagsByProblemId(problemId)
        val response = ResponseUtil.success("ProblemTags for problem retrieved successfully", problemTags, null)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/tag/{tagId}")
    fun getProblemTagsByTagId(@PathVariable tagId: Long): ResponseEntity<ApiResponse<List<ProblemTagDto>>> {
        val problemTags = problemTagService.getProblemTagsByTagId(tagId)
        val response = ResponseUtil.success("ProblemTags for tag retrieved successfully", problemTags, null)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    fun updateProblemTag(
        @PathVariable id: Long,
        @RequestParam newProblemId: Long,
        @RequestParam newTagId: Long
    ): ResponseEntity<ApiResponse<ProblemTagDto>> {
        val updatedProblemTag = problemTagService.updateProblemTag(id, newProblemId, newTagId)
        val response = ResponseUtil.success("ProblemTag updated successfully", updatedProblemTag, null)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteProblemTag(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        problemTagService.deleteProblemTag(id)
        val response = ResponseUtil.success("ProblemTag deleted successfully", Unit, null)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/problem/{problemId}/tag/{tagId}")
    fun deleteProblemTagByProblemIdAndTagId(
        @PathVariable problemId: Long,
        @PathVariable tagId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        problemTagService.deleteProblemTagByProblemIdAndTagId(problemId, tagId)
        val response = ResponseUtil.success("Tag removed from problem successfully", Unit, null)
        return ResponseEntity.ok(response)
    }

    // More semantic endpoints
    @PostMapping("/problems/{problemId}/tags/{tagId}")
    fun addTagToProblem(
        @PathVariable problemId: Long,
        @PathVariable tagId: Long
    ): ResponseEntity<ApiResponse<ProblemTagDto>> {
        val problemTagDto = problemTagService.addTagToProblem(problemId, tagId)
        val response = ResponseUtil.created("Tag added to problem successfully", problemTagDto, null)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/problems/{problemId}/tags/{tagId}")
    fun removeTagFromProblem(
        @PathVariable problemId: Long,
        @PathVariable tagId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        problemTagService.removeTagFromProblem(problemId, tagId)
        val response = ResponseUtil.success("Tag removed from problem successfully", Unit, null)
        return ResponseEntity.ok(response)
    }
}