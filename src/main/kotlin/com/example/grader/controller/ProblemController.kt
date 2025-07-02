package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.ProblemDto
import com.example.grader.entity.Difficulty
import com.example.grader.service.ProblemService
import com.example.grader.util.ResponseUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/problems")
class ProblemController(
    private val problemService: ProblemService
) {

    @PostMapping("")
    fun createProblem(
        @RequestPart("title") title: String,
        @RequestPart("difficulty") difficulty: String,
        @RequestPart("pdf") pdf: MultipartFile
    ): ResponseEntity<ApiResponse<ProblemDto>> {
        val result = problemService.addNewProblem(
            title = title,
            difficulty = Difficulty.valueOf(difficulty.uppercase()),
            pdf = pdf
        )
        val response = ResponseUtil.created("Problem created successfully", result, null)

        return ResponseEntity.status(201).body(response)
    }

    @GetMapping
    fun getAllProblems(): ResponseEntity<ApiResponse<List<ProblemDto>>> {
        val problems = problemService.listAllProblems()
        val response = ResponseUtil.success("Problems retrieved successfully", problems, null)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getProblemById(@PathVariable id: Long): ResponseEntity<ApiResponse<ProblemDto>> {
        val problem = problemService.getProblemById(id)
        val response = ResponseUtil.success("Problem retrieved successfully", problem, null)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    fun updateProblem(
        @PathVariable id: Long,
        @RequestPart("title") title: String,
        @RequestPart("difficulty") difficulty: String,
        @RequestPart("pdf") pdf: MultipartFile
    ): ResponseEntity<ApiResponse<ProblemDto>> {
        val updatedProblem = problemService.updateProblem(
            id,
            title,
            Difficulty.valueOf(difficulty.uppercase()),
            pdf
        )
        val response = ResponseUtil.success("Problem updated successfully", updatedProblem, null)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteProblem(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        problemService.deleteProblem(id)
        val response = ResponseUtil.success("Problem deleted successfully", Unit, null)
        return ResponseEntity.ok(response)
    }
}