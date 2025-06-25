package com.nixops.scraper.controller

import com.nixops.scraper.model.StudyProgram
import com.nixops.scraper.model.StudyPrograms
import com.nixops.scraper.services.scraper.ScraperScheduler
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class DebugController(private val scraperScheduler: ScraperScheduler) {

  @GetMapping("/check")
  fun check(): String {
    scraperScheduler.check()
    return "done"
  }

  @GetMapping("/study_programs/search", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun studyProgramSearch(
      @RequestParam(value = "query", defaultValue = "") query: String
  ): ResponseEntity<List<Map<String, String>>> {
    val results = transaction {
      StudyProgram.find {
            (StudyPrograms.degreeProgramName like "%$query%") or
                (StudyPrograms.programName like "%$query%")
          }
          .groupBy { it.studyId }
          .mapNotNull { (_, programs) -> programs.maxByOrNull { it.spoVersion.toIntOrNull() ?: 0 } }
          .map {
            mapOf(
                "degreeProgramName" to it.degreeProgramName,
                "studyId" to it.studyId.toString(),
            )
          }
    }

    return ResponseEntity.ok(results)
  }
}
