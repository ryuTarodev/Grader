package com.example.grader.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Instant

@Entity
@Table(name = "app_users")
data class AppUser(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "app_user_id_seq", sequenceName = "app_user_id_seq", allocationSize = 1)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var appUsername: String = "",

    @Column(nullable = false)
    var clientPassword: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.ROLE_USER,

    @Column(nullable = false)
    var profilePicture: String = "",

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant? = null,


    ): UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(SimpleGrantedAuthority(role.name))
    }


    override fun getPassword(): String = clientPassword

    override fun getUsername(): String = appUsername

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

}