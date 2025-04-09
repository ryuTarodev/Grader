package com.example.grader.repository

import com.example.grader.entity.AppUser
import org.springframework.data.jpa.repository.JpaRepository

interface AppUserRepository : JpaRepository<AppUser, Long> {
    fun findAppUserByAppUsername (username:String): AppUser?
    fun existsAppUserByAppUsername(appUsername: String): Boolean

}