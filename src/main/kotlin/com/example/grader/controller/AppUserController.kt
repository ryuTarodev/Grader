package com.example.grader.controller

import com.example.grader.dto.ApiResponse
import com.example.grader.dto.AppUserDto
import com.example.grader.service.AppUserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/users")
class AppUserController(private val appUserService: AppUserService) {

    @GetMapping("")
    fun getAppUsers(): ResponseEntity<ApiResponse<List<AppUserDto>>> {
        val response: ApiResponse<List<AppUserDto>> = appUserService.getAppUsers()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getAppUserById(@PathVariable id: Long): ResponseEntity<ApiResponse<AppUserDto>> {
        val response: ApiResponse<AppUserDto> = appUserService.getUserById(id)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{id}/upload")
    fun uploadProfile(@PathVariable id: Long, @RequestParam png: MultipartFile): ResponseEntity<ApiResponse<AppUserDto>> {
        val response: ApiResponse<AppUserDto> = appUserService.uploadUserProfile(id, png)
        return ResponseEntity.ok(response)
    }

}