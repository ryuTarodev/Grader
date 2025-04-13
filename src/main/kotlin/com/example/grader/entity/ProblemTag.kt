package com.example.grader.entity

import jakarta.persistence.*
import java.time.Instant

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
    var tag: Tag = Tag(),

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant? = null


)