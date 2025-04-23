package com.example.grader.dto.RequstResponse

import com.example.grader.entity.Difficulty
import org.springframework.web.multipart.MultipartFile

class ProblemRequest (
    val title: String,
    val difficulty: Difficulty,
    val pdf: MultipartFile
)