package com.example.grader.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "problems")
data class Problem(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "problem_id_seq")
    @SequenceGenerator(name = "problem_id_seq", sequenceName = "problem_id_seq", allocationSize = 1)
    var id: Long? = 0,

    @Column(nullable = false)
    var title: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var difficulty: Difficulty = Difficulty.EASY,

    @Column(nullable = false, columnDefinition = "TEXT")
    var pdf: String = "",

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant? = null
)