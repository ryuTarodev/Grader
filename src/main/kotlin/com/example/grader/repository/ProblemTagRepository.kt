package com.example.grader.repository

import com.ryutaro.grader.entity.ProblemTag
import org.springframework.data.jpa.repository.JpaRepository

interface ProblemTagRepository : JpaRepository<ProblemTag, Long> {
}