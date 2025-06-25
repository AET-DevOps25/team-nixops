package com.nixops.scraper

import com.nixops.scraper.model.*
import com.nixops.scraper.services.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.bind.annotation.*

@SpringBootApplication
@EnableScheduling
@RestController
class ScraperApplication(
    private val semesterService: SemesterService,
    private val moduleService: ModuleService
) {
  @GetMapping("/courses", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun courses(
      @RequestParam(value = "study_id", defaultValue = "163016030") studyId: Long,
      @RequestParam(value = "semester", defaultValue = "2025s") semesterKey: String,
  ): ResponseEntity<String> {
    val semester =
        semesterService.getSemester(semesterKey) ?: return ResponseEntity.notFound().build()
    val modules = moduleService.getModules(studyId, semester)

    for (module in modules) {
      println("module: ${module.moduleCode} ${module.moduleTitle} ${module.id}")
    }

    println("found ${modules.count()} modules")

    return ResponseEntity.ok("done")
  }
}

fun main(args: Array<String>) {
  runApplication<ScraperApplication>(*args)
}
