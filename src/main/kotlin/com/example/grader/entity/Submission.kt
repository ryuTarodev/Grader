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
@Table(name = "submissions")
data class Submission(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val appUser: AppUser = AppUser(),

    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    var problem: Problem = Problem(),

    @Column
    val code: String? = null,

    @Column
    val score: Float? = 0f,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: Status = Status.PENDING,

    @Column(name = "submitted_at", nullable = false, updatable = false)
    val submittedAt: Instant = Instant.now()
)