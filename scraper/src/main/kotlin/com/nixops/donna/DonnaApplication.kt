package com.nixops.scraper

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.*

@SpringBootApplication
@RestController
class ScraperApplication {
  @GetMapping("/hello")
  fun hello(@RequestParam(value = "name", defaultValue = "World") name: String): String {
    return "Hello $name!"
  }
}

fun main(args: Array<String>) {
	runApplication<ScraperApplication>(*args)
}
