package com.example.grader.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig {

    @Bean
    fun setupCorsConfigurationSource(): UrlBasedCorsConfigurationSource { // create and return cors configuration
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("*")                                                // allow all domain
            allowedMethods = listOf("HEAD", "GET", "POST", "PUT", "PATCH", "DELETE")    // allow method
            allowedHeaders = listOf("*")                                                // allow all header request
            allowCredentials = true                                                     // allow credentials like cookie
        }
        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)     // use config apply to every url pattern
        }

        return source
    }
}