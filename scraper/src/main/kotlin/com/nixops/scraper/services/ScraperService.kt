package com.nixops.scraper.services

import com.nixops.scraper.model.getTimeSinceLastUpdated
import com.nixops.scraper.model.setLastUpdated
import java.time.Duration
import org.springframework.stereotype.Service

@Service
class ScraperService(private val semesterService: SemesterService) {
  fun scrapeSemesters() {
    semesterService.getSemesters()
  }

  fun checkSemesters() {
    val semesterLastUpdate = getTimeSinceLastUpdated("semesters")

    if (semesterLastUpdate == null || semesterLastUpdate > Duration.ofHours(2)) {
      println("should update semesters")

      scrapeSemesters()
      setLastUpdated("semesters")
    }
  }

  fun check() {
    checkSemesters()
  }
}
