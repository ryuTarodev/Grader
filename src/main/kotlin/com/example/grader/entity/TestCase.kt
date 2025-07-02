package com.example.grader.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "test_cases")
data class TestCase(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "test_case_id_seq")
    @SequenceGenerator(name = "test_case_id_seq", sequenceName = "test_case_id_seq", allocationSize = 1)
    val id: Long? = 0,

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