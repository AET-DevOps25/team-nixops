package com.nixops.scraper.services

import com.nixops.scraper.model.*
import com.nixops.scraper.tum_api.campus.api.CampusCurriculumApiClient
import com.nixops.scraper.tum_api.nat.api.NatSemesterApiClient
import java.time.Duration
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class ScraperService(
    private val semesterApiClient: NatSemesterApiClient,
    private val curriculumApiClient: CampusCurriculumApiClient,
) {
  fun scrapeSemester(semesterKey: String): Semester {
    return transaction {
      val existing = Semester.findById(semesterKey)
      if (existing != null) {
        existing
      } else {
        val natSemester = semesterApiClient.getSemester(semesterKey)
        println("Saving semester with key: ${natSemester.semesterKey}")

        val semester =
            Semester.new(natSemester.semesterKey) {
              semesterTag = natSemester.semesterTag
              semesterTitle = natSemester.semesterTitle
              semesterIdTumOnline = natSemester.semesterIdTumOnline
            }

        semester
      }
    }
  }

  fun scrapeSemesters() {
    val semesters = semesterApiClient.getSemesters()
    semesters.map { scrapeSemester(it.semesterKey) }
  }

  fun scrapeCurricula(tumId: Int): List<Curriculum> {
    return transaction {
      curriculumApiClient.getCurriculaForSemester(tumId).map {
        println("Saving curriculum with name: ${it.name}")

        val existing = Curriculum.findById(it.id)
        if (existing != null) {
          existing.name = it.name
          existing
        } else {
          Curriculum.new(it.id) { name = it.name }
        }
      }
    }
  }

  fun scrapeCurricula() {
    transaction {
      Semester.all().forEach { semester ->
        semester.semesterIdTumOnline?.let { scrapeCurricula(it) }
      }
    }
  }

  fun check(name: String, scrape: () -> Unit) {
    val lastUpdated = getTimeSinceLastUpdated(name)
    if (lastUpdated == null || lastUpdated > Duration.ofHours(2)) {
      println("should update $name")
      scrape()
      setLastUpdated(name)
    }
  }

  fun check() {
    check("semesters", ::scrapeSemesters)
    check("curricula", ::scrapeCurricula)
  }
}
