package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.ProblemTagDto
import com.example.grader.entity.ProblemTag
import com.example.grader.service.ProblemTagService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/problemTags")
class ProblemTagController(
    private val problemTagService: ProblemTagService
) {

    // Create a new ProblemTag
    @PostMapping("/problems/{problemId}/tags/{tagId}")
    fun createProblemTag(
        @PathVariable problemId: Long,
        @PathVariable tagId: Long
    ): ResponseEntity<ApiResponse<ProblemTagDto>> {
        val response = problemTagService.createProblemTag(problemId, tagId)
        return ResponseEntity.ok(response)
    }

    // Get ProblemTags by Problem ID
    @GetMapping("/problems/{problemId}")
    fun getProblemTagsByProblemId(
        @PathVariable problemId: Long
    ): ResponseEntity<ApiResponse<List<ProblemTagDto>>> {
        val response = problemTagService.getProblemTagsByProblemId(problemId)
        return ResponseEntity.ok(response)
    }

    // Get ProblemTags by Tag ID
    @GetMapping("/tags/{tagId}")
    fun getProblemTagsByTagId(
        @PathVariable tagId: Long
    ): ResponseEntity<ApiResponse<List<ProblemTagDto>>> {
        val response = problemTagService.getProblemTagsByTagId(tagId)
        return ResponseEntity.ok(response)
    }

    // Update a ProblemTag
    @PutMapping("/{id}")
    fun updateProblemTag(
        @PathVariable id: Long,
        @RequestParam newProblemId: Long,
        @RequestParam newTagId: Long
    ): ResponseEntity<ApiResponse<ProblemTagDto>> {
        val response = problemTagService.updateProblemTag(id, newProblemId, newTagId)
        return ResponseEntity.ok(response)
    }

    // Delete a ProblemTag
    @DeleteMapping("/{id}")
    fun deleteProblemTag(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        val response = problemTagService.deleteProblemTag(id)
        return ResponseEntity.ok(response)
    }
    @DeleteMapping("/problems/{problemId}/tags/{tagId}")
    fun deleteProblemTag(
        @PathVariable problemId: Long,
        @PathVariable tagId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        val response = problemTagService.deleteProblemTagByProblemIdAndTagId(problemId, tagId)
        return ResponseEntity.ok(response)
    }
}