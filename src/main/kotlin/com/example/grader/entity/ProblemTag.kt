package com.example.grader.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "problem_tags")
data class ProblemTag(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "problem_tag_id_seq")
    @SequenceGenerator(name = "problem_tag_id_seq", sequenceName = "problem_tag_id_seq", allocationSize = 1)
    var id: Long? = 0,


    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    var problem: Problem = Problem(),

    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false)
    var tag: Tag = Tag(),

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant? = null


)