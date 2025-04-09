package com.example.grader.repository

import com.ryutaro.grader.entity.Problem
import org.springframework.data.jpa.repository.JpaRepository

interface ProblemRepository : JpaRepository<Problem, Long> {
}