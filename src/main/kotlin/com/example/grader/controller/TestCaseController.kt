package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.RequstResponse.TestCaseRequest
import com.example.grader.dto.TestCaseDto
import com.example.grader.service.TestCaseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/problems/{problemId}/testcases")
class TestCaseController(private val testCaseService: TestCaseService) {

    @PostMapping("")
    fun addNewTestCase(@PathVariable problemId: Long,
                       @RequestBody testCaseRequest: TestCaseRequest): ResponseEntity<ApiResponse<TestCaseDto>> {
        val response: ApiResponse<TestCaseDto> = testCaseService.createTestCase(testCaseRequest, problemId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("")
    fun getTestCases(@PathVariable problemId: Long): ResponseEntity<ApiResponse<List<TestCaseDto>>> {
        val response = testCaseService.getTestCasesByProblemId(problemId)
        return ResponseEntity.ok(response)
    }
    @GetMapping("/{id}")
    fun getTestCaseById(@PathVariable problemId: Long,
                        @PathVariable id: Long): ResponseEntity<ApiResponse<TestCaseDto>> {
        val response = testCaseService.getTestCaseById(id)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    fun updateTestCase(@PathVariable id: Long,
                       @PathVariable problemId: Long,
                       @RequestBody testCaseRequest: TestCaseRequest): ResponseEntity<ApiResponse<TestCaseDto>> {
        val response: ApiResponse<TestCaseDto> = testCaseService.updateTestCase(id, testCaseRequest, problemId)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteTestCase(@PathVariable problemId: Long,
                       @PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        val response: ApiResponse<Unit> = testCaseService.deleteTestCase(id)
        return ResponseEntity.ok(response)
    }
}