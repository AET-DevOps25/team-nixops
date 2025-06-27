package com.nixops.schedulingEngine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.*

@SpringBootApplication
@RestController
class SchedulingEngineApplication {
  @GetMapping("/hello")
  fun hello(@RequestParam(value = "name", defaultValue = "World") name: String): String {
    return "Hello $name!"
  }
}

fun main(args: Array<String>) {
  runApplication<SchedulingEngineApplication>(*args)
}
