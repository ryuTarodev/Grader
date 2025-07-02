package com.example.grader.security

import com.example.grader.entity.AppUser
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userService: UserDetailsService
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val AUTH_HEADER = "Authorization"
        private const val BEARER_TOKEN_PREFIX = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val correlationId = request.getHeader("X-Correlation-ID") ?: UUID.randomUUID().toString()
            MDC.put("correlationId", correlationId)
            response.setHeader("X-Correlation-ID", correlationId)

            val authHeader: String? = request.getHeader(AUTH_HEADER)

            if (authHeader != null && authHeader.startsWith(BEARER_TOKEN_PREFIX)) {
                val jwt = authHeader.substring(BEARER_TOKEN_PREFIX.length).trim()
                log.debug("JWT found in header: $jwt")

                if (SecurityContextHolder.getContext().authentication == null) {
                    try {
                        val username = jwtService.extractUsername(jwt)
                        log.debug("Extracted username from JWT: $username")

                        val userDetails = userService.loadUserByUsername(username)
                        log.debug("Loaded user details for username: $username")

                        val role = jwtService.extractAuthorities(jwt)
                        log.debug("Extracted role from JWT: ${role.name}")

                        if (jwtService.isTokenValid(jwt, userDetails)) {
                            val authorities = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
                            val authToken = UsernamePasswordAuthenticationToken(
                                userDetails, null, authorities
                            ).apply {
                                details = WebAuthenticationDetailsSource().buildDetails(request)
                            }

                            SecurityContextHolder.getContext().authentication = authToken
                            log.info("Authentication set for user: $username with role: ${role.name}")

                            val userId = (userDetails as? AppUser)?.id?.toString()
                            if (userId != null) {
                                MDC.put("userId", userId)
                                log.debug("User ID added to MDC: $userId")
                            }
                        } else {
                            log.warn("JWT token is invalid for user: $username")
                        }

                    } catch (e: Exception) {
                        log.error("Error processing JWT: ${e.message}", e)
                        response.status = HttpServletResponse.SC_UNAUTHORIZED
                        response.writer.write("Invalid JWT token")
                        return
                    }
                }
            } else {
                log.debug("No valid Authorization header found.")
            }

            filterChain.doFilter(request, response)
        } finally {
            MDC.remove("userId")
            MDC.remove("correlationId")
        }
    }
}