package com.example.grader.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "submissions")
data class Submission(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "submission_id_seq")
    @SequenceGenerator(name = "submission_id_seq", sequenceName = "submission_id_seq", allocationSize = 1)
    val id: Long? = 0,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val appUser: AppUser = AppUser(),

    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    var problem: Problem = Problem(),

    @Column
    var code: String? = null,

    @Column
    var language: String? = null,

    @Column
    var score: Float? = 0f,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: Status = Status.PENDING,

    @Column(name = "submitted_at", nullable = false, updatable = false)
    val submittedAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant? = null
)