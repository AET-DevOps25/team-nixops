package com.nixops.scraper.services

import com.nixops.scraper.model.Semester
import com.nixops.scraper.services.scraper.SemesterScraper
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class SemesterService(private val semesterScraper: SemesterScraper) {
  fun getSemesters(): List<Semester> {
    return transaction { Semester.all().toList() }
  }

  fun getSemester(semesterKey: String): Semester? {
    return semesterScraper.scrapeSemester(semesterKey)
  }
}
