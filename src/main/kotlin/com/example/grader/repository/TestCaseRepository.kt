package com.example.grader.repository

import com.example.grader.entity.TestCase
import org.springframework.data.jpa.repository.JpaRepository

interface TestCaseRepository : JpaRepository<TestCase, Long> {
    fun findByProblemId(problemId: Long): List<TestCase>
    fun findByProblemIdOrderByIdAsc(problemId: Long): List<TestCase>?
    fun countByProblemId(problemId: Long): Int
    fun deleteAllByProblemId(problemId: Long)
}