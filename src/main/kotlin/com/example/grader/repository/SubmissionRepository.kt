package com.example.grader.repository

import com.example.grader.entity.Submission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface SubmissionRepository : JpaRepository<Submission, Long> {
    @Modifying
    @Query("DELETE FROM Submission s WHERE s.problem.id = :problemId AND s.appUser.id = :appUserId")
    fun deleteAllByProblemIdAndAppUserId(problemId: Long, appUserId: Long): Int
    fun findAllByProblemIdAndAppUserId(appUserId: Long, problemId: Long): List<Submission>?
}