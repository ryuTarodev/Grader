package com.example.grader.repository

import com.example.grader.entity.Submission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface SubmissionRepository : JpaRepository<Submission, Long> {
    fun findAllByProblemIdAndAppUserId(problemId: Long, appUserId: Long): List<Submission>
    fun findAllByAppUserId(appUserId: Long): List<Submission>
    fun findAllByProblemId(problemId: Long): List<Submission>
    fun countByProblemIdAndAppUserId(problemId: Long, appUserId: Long): Long
    fun deleteAllByProblemIdAndAppUserId(problemId: Long, appUserId: Long)
}