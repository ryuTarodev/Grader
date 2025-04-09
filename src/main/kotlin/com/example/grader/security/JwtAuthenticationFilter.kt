package com.example.grader.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userService: UserDetailsService
): OncePerRequestFilter() {

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
        val authHeader: String? = request.getHeader(AUTH_HEADER)
        if (authHeader != null && authHeader.startsWith(BEARER_TOKEN_PREFIX)){
            val jwt = authHeader.substring(BEARER_TOKEN_PREFIX.length).trim()
            if (SecurityContextHolder.getContext().authentication == null) {
                try {
                    val userName: String = jwtService.extractUsername(jwt)
                    val userDetails: UserDetails = userService.loadUserByUsername(userName)
                    if(jwtService.isTokenValid(token = jwt, userDetails =  userDetails)) {
                        val authToken = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                            .apply { details = WebAuthenticationDetailsSource().buildDetails(request) }
                        SecurityContextHolder.getContext().authentication = authToken
                    }
                } catch (e: Exception) {
                    log.error("Error processing JWT: ${e.message}")
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    response.writer.write("Invalid JWT token")
                    return
                }


            }
        }
        filterChain.doFilter(request, response)
    }
}