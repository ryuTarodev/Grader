package com.example.grader

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class GraderApplication

fun main(args: Array<String>) {
    runApplication<GraderApplication>(*args)
}
