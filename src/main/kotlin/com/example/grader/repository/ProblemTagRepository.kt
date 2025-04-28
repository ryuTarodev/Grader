package com.example.grader.repository

import com.example.grader.entity.ProblemTag
import org.springframework.data.jpa.repository.JpaRepository

interface ProblemTagRepository : JpaRepository<ProblemTag, Long> {
    fun findByProblemId(problemId: Long): List<ProblemTag>?
    fun findByTagId(tagId: Long): List<ProblemTag>?
}