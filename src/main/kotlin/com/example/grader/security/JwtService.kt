package com.example.grader.security

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

    // Extract claims from the token
    private fun extractAllClaims(token: String): Claims =
        try {
            Jwts.parserBuilder()
                .setSigningKey(jwtKey.secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: Exception) {
            log.error("Failed to extract claims from token: ${e.message}")
            throw JwtException("Invalid token")  // Specific exception for JWT issues
        }
    // Extract the username from the token
    fun extractUsername(token: String): String =
        extractAllClaims(token).subject

    // Check if the token is expired
    private fun isTokenExpired(token: String): Boolean =
        extractAllClaims(token).expiration.before(Date())

    // Check if the token is valid for the given user
    fun isTokenValid(token: String, userDetails: UserDetails): Boolean =
        extractUsername(token) == userDetails.username && !isTokenExpired(token)


    // Internal method to generate the token
    private fun generateToken(userDetails: UserDetails, expirationTime: Long): String =
        Jwts.builder()
            .setSubject(userDetails.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expirationTime))
            .signWith(jwtKey.secretKey)
            .compact()

    // Generate access token with 1-day expiration
    fun generateAccessToken(userDetails: UserDetails): String =
        generateToken(userDetails, EXPIRATION_ONE_DAY)

    // Generate refresh token with 7-days expiration
    fun generateRefreshToken(userDetails: UserDetails): String =
        generateToken(userDetails, EXPIRATION_SEVEN_DAYS)

}
