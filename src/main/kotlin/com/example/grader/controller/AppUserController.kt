package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.AppUserDto
import com.example.grader.entity.AppUser
import com.example.grader.service.AppUserService
import com.example.grader.service.ClientSessionService
import com.example.grader.util.ResponseUtil
import com.example.grader.util.toAppUserDTO
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/users")
class AppUserController(
    private val appUserService: AppUserService,
    private val clientSessionService: ClientSessionService
) {
    private val logger = LoggerFactory.getLogger(javaClass)


    @GetMapping("")
    fun getAppUsers(): ResponseEntity<ApiResponse<List<AppUserDto>>> {
        val appUser = appUserService.getAppUsers()
        val response = ResponseUtil.created("AppUser created successfully", appUser, null)
        return ResponseEntity.ok(response)
    }
    @GetMapping("/me")
    fun getCurrentUser(): ResponseEntity<ApiResponse<ApiResponse<AppUserDto>>> {

        val appUser = clientSessionService.getCurrentUserResponse()
        val response = ResponseUtil.created("AppUser fetch successfully", appUser, null)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getAppUserById(@PathVariable id: Long): ResponseEntity<ApiResponse<AppUserDto>> {
        val appUser = appUserService.getUserById(id)
        val response = ResponseUtil.success("AppUser fetch successfully", appUser, null)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{id}/upload")
    fun uploadProfile(@PathVariable id: Long, @RequestParam png: MultipartFile): ResponseEntity<ApiResponse<AppUserDto>> {
        val appUser  = appUserService.uploadUserProfile(id, png)
        val response = ResponseUtil.success("AppUser upload successfully", appUser, null)
        return ResponseEntity.ok(response)
    }

}