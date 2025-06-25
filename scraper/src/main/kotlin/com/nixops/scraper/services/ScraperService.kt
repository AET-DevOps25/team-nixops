package com.nixops.scraper.services

import org.springframework.stereotype.Service

@Service
class ScraperService(private val semesterService: SemesterService) {
  fun scrapeSemesters() {
    val semesters = semesterService.getSemesters()
  }
}
