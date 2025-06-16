package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.ProblemDto
import com.example.grader.dto.RequesttResponse.ProblemRequest
import com.example.grader.service.ProblemService
import com.example.grader.util.ResponseUtil
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/problems")
class ProblemController(
    private val problemService: ProblemService
) {

    @PostMapping
    fun createProblem(@Valid @RequestBody problemRequest: ProblemRequest): ResponseEntity<ApiResponse<ProblemDto>> {
        val problemDto = problemService.addNewProblem(problemRequest)
        val response = ResponseUtil.created("Problem created successfully", problemDto, null)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
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
        @Valid @RequestBody problemRequest: ProblemRequest
    ): ResponseEntity<ApiResponse<ProblemDto>> {
        val updatedProblem = problemService.updateProblem(id, problemRequest)
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