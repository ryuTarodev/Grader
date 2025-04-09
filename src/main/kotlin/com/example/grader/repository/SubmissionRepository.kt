package com.example.grader.repository

import com.ryutaro.grader.entity.Submission
import org.springframework.data.jpa.repository.JpaRepository

interface SubmissionRepository : JpaRepository<Submission, Long> {
}