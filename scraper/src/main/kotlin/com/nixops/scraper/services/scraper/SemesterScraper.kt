package com.nixops.scraper.services.scraper

import com.nixops.scraper.model.*
import com.nixops.scraper.tum_api.nat.api.NatSemesterApiClient
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class SemesterScraper(
    private val semesterApiClient: NatSemesterApiClient,
) {
  fun scrapeSemester(semesterKey: String): Semester {
    return transaction {
      val natSemester = semesterApiClient.getSemester(semesterKey)
      println("Saving semester with key: ${natSemester.semesterKey}")

      val existing = Semester.findById(natSemester.semesterKey)
      if (existing != null) {
        existing.semesterTag = natSemester.semesterTag
        existing.semesterTitle = natSemester.semesterTitle
        existing.semesterIdTumOnline = natSemester.semesterIdTumOnline
        existing
      } else {
        Semester.new(natSemester.semesterKey) {
          semesterTag = natSemester.semesterTag
          semesterTitle = natSemester.semesterTitle
          semesterIdTumOnline = natSemester.semesterIdTumOnline
        }
      }
    }
  }

  fun scrapeSemesters() {
    val semesters = semesterApiClient.getSemesters()
    semesters.map { scrapeSemester(it.semesterKey) }
  }
}
