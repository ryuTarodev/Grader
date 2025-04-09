package com.example.grader.service

import com.example.grader.entity.AppUser
import com.example.grader.error.UserNotFoundException
import com.example.grader.repository.AppUserRepository
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class ClientSessionService(private val repository: AppUserRepository) {

    fun retrieveAuthentication(): Authentication? {
        return SecurityContextHolder.getContext().authentication
    }

    fun findCurrentSessionUser(): AppUser {
        val authentication: Authentication =
            retrieveAuthentication() ?: throw UserNotFoundException("Authenticated user not found")

        val username = when (val principal = authentication.principal) {
            is UserDetails -> principal.username
            else -> principal.toString()
        }

        return repository.findAppUserByAppUsername(username)
            ?: throw UserNotFoundException("User not found with username: $username")
    }

    fun getAuthenticatedUser(): AppUser {
        val authentication: Authentication? = retrieveAuthentication()
        return authentication?.principal as AppUser
    }
}