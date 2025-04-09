package com.example.grader.security

import com.example.grader.repository.AppUserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class AuthenticationConfig(private val repository: AppUserRepository) {

    @Bean
    fun getUsername() =
        UserDetailsService { username ->
            repository.findAppUserByAppUsername(username)
        }

    @Bean
    fun getPasswordEncoder(): PasswordEncoder = Argon2PasswordEncoder(16, 32, 8, 1024 * 128, 2)

    @Bean
    fun getAuthenticationProvider(): AuthenticationProvider {
        return DaoAuthenticationProvider().apply {
            this.setUserDetailsService(getUsername())
            this.setPasswordEncoder(getPasswordEncoder())
        }
    }

    @Bean
    fun getAuthenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager? = authConfig.authenticationManager
}