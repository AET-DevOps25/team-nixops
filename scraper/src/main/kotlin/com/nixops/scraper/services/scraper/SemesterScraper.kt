package com.nixops.scraper.services.scraper

import com.nixops.scraper.extensions.genericUpsert
import com.nixops.scraper.model.*
import com.nixops.scraper.tum_api.nat.api.NatSemesterApiClient
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class SemesterScraper(
    private val semesterApiClient: NatSemesterApiClient,
) {
  fun scrapeSemester(semesterKey: String): Semester? {
    return transaction {
      val natSemester = semesterApiClient.getSemester(semesterKey) ?: return@transaction null
      logger.debug("Saving semester with key: {} {}", natSemester.semesterKey, natSemester)

      Semesters.genericUpsert(Semester) {
        it[Semesters.id] = natSemester.semesterKey
        it[Semesters.semesterTag] = natSemester.semesterTag
        it[Semesters.semesterTitle] = natSemester.semesterTitle
        it[Semesters.semesterIdTumOnline] = natSemester.semesterIdTumOnline
      }
    }
  }

  fun scrapeSemesters() {
    val semesters = semesterApiClient.getSemesters()
    semesters.map { scrapeSemester(it.semesterKey) }
  }
}
