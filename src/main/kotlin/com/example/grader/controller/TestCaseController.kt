package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.RequstResponse.UpdateTestCaseRequest
import com.example.grader.dto.TestCaseDto
import com.example.grader.service.TestCaseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/problems/{problemId}/testcases")
class TestCaseController(private val testCaseService: TestCaseService) {

    @PostMapping("")
    fun addNewTestCase(@PathVariable problemId: Long,
                       @RequestBody testCaseRequest: UpdateTestCaseRequest): ResponseEntity<ApiResponse<TestCaseDto>> {
        val response: ApiResponse<TestCaseDto> = testCaseService.createTestCase(testCaseRequest.copy(problemId = problemId))
        return ResponseEntity.ok(response)
    }

    @GetMapping("")
    fun getTestCases(@PathVariable problemId: Long): ResponseEntity<ApiResponse<List<TestCaseDto>>> {
        val response = testCaseService.getTestCasesByProblemId(problemId)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    fun updateTestCase(@PathVariable id: Long,
                       @RequestBody testCaseRequest: UpdateTestCaseRequest): ResponseEntity<ApiResponse<TestCaseDto>> {
        val response: ApiResponse<TestCaseDto> = testCaseService.updateTestCase(id, testCaseRequest)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteTestCase(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        val response: ApiResponse<Unit> = testCaseService.deleteTestCase(id)
        return ResponseEntity.ok(response)
    }
}