package com.example.grader.security

import com.example.grader.entity.AppUser
import com.example.grader.entity.Role
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date
import java.util.concurrent.TimeUnit

@Service
class JwtService(private val jwtKey: JwtKey) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val EXPIRATION_ONE_DAY = TimeUnit.DAYS.toMillis(1)
        private val EXPIRATION_SEVEN_DAYS = TimeUnit.DAYS.toMillis(7)
    }

    private fun extractAllClaims(token: String): Claims =
        try {
            Jwts.parserBuilder()
                .setSigningKey(jwtKey.secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: Exception) {
            log.error("Failed to extract claims from token: ${e.message}")
            throw JwtException("Invalid token")
        }

    fun extractUsername(token: String): String =
        extractAllClaims(token).subject

    fun extractAuthorities(token: String): Role =
        Role.valueOf(extractAllClaims(token)["role"].toString())

    fun isTokenExpired(token: String): Boolean =
        extractAllClaims(token).expiration.before(Date())

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean =
        extractUsername(token) == userDetails.username && !isTokenExpired(token)

    fun isTokenValid(token: String): Boolean {
        return try {
            !isTokenExpired(token)
        } catch (e: Exception) {
            log.warn("Token validation failed: ${e.message}")
            false
        }
    }

    private fun generateToken(userDetails: UserDetails, expirationTime: Long): String =
        Jwts.builder()
            .setSubject(userDetails.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expirationTime))
            .claim("role", (userDetails as AppUser).role.name)
            .signWith(jwtKey.secretKey)
            .compact()

    fun generateAccessToken(userDetails: UserDetails): String =
        generateToken(userDetails, EXPIRATION_ONE_DAY)

    fun generateRefreshToken(userDetails: UserDetails): String =
        generateToken(userDetails, EXPIRATION_SEVEN_DAYS)
}