package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.RequestResponse.TestCaseRequest
import com.example.grader.dto.TestCaseDto
import com.example.grader.service.TestCaseService
import com.example.grader.util.ResponseUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/problems/{problemId}/testcases")
class TestCaseController(private val testCaseService: TestCaseService) {

    @PostMapping("")
    fun addNewTestCase(@PathVariable problemId: Long,
                       @RequestBody testCaseRequest: TestCaseRequest): ResponseEntity<ApiResponse<TestCaseDto>> {
        val testCase = testCaseService.createTestCase(testCaseRequest, problemId)
        val response = ResponseUtil.created("TestCase created successfully", testCase, null)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("")
    fun getTestCases(@PathVariable problemId: Long): ResponseEntity<ApiResponse<List<TestCaseDto>>> {
        val testCases = testCaseService.getTestCasesByProblemId(problemId)
        val response = ResponseUtil.success("TestCase found successfully", testCases, null)
        return ResponseEntity.ok(response)
    }
    @GetMapping("/{id}")
    fun getTestCaseById(@PathVariable problemId: Long,
                        @PathVariable id: Long): ResponseEntity<ApiResponse<TestCaseDto>> {
        val testCase = testCaseService.getTestCaseById(id)
        val response = ResponseUtil.success("TestCase found successfully", testCase, null)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    fun updateTestCase(@PathVariable id: Long,
                       @PathVariable problemId: Long,
                       @RequestBody testCaseRequest: TestCaseRequest): ResponseEntity<ApiResponse<TestCaseDto>> {
        val testCase: TestCaseDto = testCaseService.updateTestCase(id,problemId, testCaseRequest)
        val response = ResponseUtil.success("TestCase updated successfully", testCase, null)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteTestCase(@PathVariable problemId: Long,
                       @PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        val testCase = testCaseService.deleteTestCase(id)
        val response = ResponseUtil.success("TestCase deleted successfully", testCase, null)
        return ResponseEntity.ok(response)
    }
}