package com.example.grader.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "test_cases")
data class TestCase(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    var problem: Problem = Problem(),

    @Column(nullable = false)
    var input: String = "",

    @Column(nullable = false)
    var output: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: Type = Type.PRIVATE,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant? = null
)