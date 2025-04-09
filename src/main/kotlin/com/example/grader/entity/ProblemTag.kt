package com.example.grader.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "problem_tags")
data class ProblemTag(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    val problem: Problem = Problem(),

    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false)
    var tag: Tag = Tag()
)