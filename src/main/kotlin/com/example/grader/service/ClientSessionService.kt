package com.example.grader.service

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.AppUserDto
import com.example.grader.entity.AppUser
import com.example.grader.error.UserNotFoundException
import com.example.grader.repository.AppUserRepository
import com.example.grader.util.ResponseUtil
import com.example.grader.util.toAppUserDTO
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class ClientSessionService(private val repository: AppUserRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun retrieveAuthentication(): Authentication? {
        return SecurityContextHolder.getContext().authentication
    }

    fun getCurrentUser(): AppUserDto {
        val authentication = retrieveAuthentication()
            ?: throw UserNotFoundException("Authenticated user not found")

        val username = when (val principal = authentication.principal) {
            is UserDetails -> principal.username
            else -> principal.toString()
        }

        val user = repository.findAppUserByAppUsername(username)
            ?: throw UserNotFoundException("User not found with username: $username")

        logger.info("Current authenticated user: $username")

        return user.toAppUserDTO()
    }
}