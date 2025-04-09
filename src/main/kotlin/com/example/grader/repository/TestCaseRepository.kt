package com.example.grader.repository

import com.ryutaro.grader.entity.TestCase
import org.springframework.data.jpa.repository.JpaRepository

interface TestCaseRepository : JpaRepository<TestCase, Long> {
}