package com.nixops.scraper

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.*

import com.example.api.StudyProgramsApi
import org.openapitools.client.infrastructure.ApiClient

@SpringBootApplication
@RestController
class ScraperApplication {
  @GetMapping("/hello")
  fun hello(@RequestParam(value = "name", defaultValue = "World") name: String): String {

    val programsApi = StudyProgramsApi(
        basePath = "https://api.srv.nat.tum.de"
    );

    try {
        val response = programsApi.readProgramsCombined()
        println("Programs: $response")
    } catch (e: Exception) {
        println("API error: ${e.message}")
    }

    return "Hello $name!"
  }
}

fun main(args: Array<String>) {
	runApplication<ScraperApplication>(*args)
}
