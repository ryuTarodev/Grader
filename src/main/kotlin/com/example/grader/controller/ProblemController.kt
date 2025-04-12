package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.ProblemDto
import com.example.grader.entity.Difficulty
import com.example.grader.service.ProblemService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/problems")
class ProblemController(private val problemService: ProblemService) {

    @PostMapping("")
    fun addNewProblem(
        @RequestParam("title") title: String,
        @RequestParam("difficulty") difficulty: Difficulty,
        @RequestParam("pdf") pdf: MultipartFile
    ): ResponseEntity<ApiResponse<ProblemDto>> {
        val response: ApiResponse<ProblemDto> = problemService.addNewProblem(title, difficulty, pdf)
        return ResponseEntity.ok(response)
    }

    @GetMapping("")
    fun getProblems(): ResponseEntity<ApiResponse<List<ProblemDto>>> {
        val response = problemService.listAllProblems()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getProblemById(@PathVariable id: Long): ResponseEntity<ApiResponse<ProblemDto>> {
        val response = problemService.getProblemById(id)
        return ResponseEntity.ok(response)
    }
    @PutMapping("/{id}")
    fun updateProblem(
        @PathVariable id: Long,
        @RequestParam("title") title: String,
        @RequestParam("difficulty") difficulty: Difficulty,
        @RequestParam(value = "pdf", required = false) pdf: MultipartFile?
    ): ResponseEntity<ApiResponse<ProblemDto>> {
        val response = problemService.updateProblem(id, title, difficulty, pdf)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteProblem(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        val response = problemService.deleteProblem(id)
        return ResponseEntity.ok(response)
    }
}