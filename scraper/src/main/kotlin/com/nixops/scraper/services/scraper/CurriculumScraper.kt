package com.nixops.scraper.services.scraper

import com.nixops.scraper.model.*
import com.nixops.scraper.tum_api.campus.api.CampusCurriculumApiClient
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class CurriculumScraper(
    private val curriculumApiClient: CampusCurriculumApiClient,
) {
  fun scrapeCurricula(tumId: Int): List<Curriculum> {
    return transaction {
      curriculumApiClient.getCurriculaForSemester(tumId).map { apiCurriculum ->
        logger.debug("Saving curriculum with name: ${apiCurriculum.name}")

        val existing = Curriculum.findById(apiCurriculum.id)
        if (existing != null) {
          existing.name = apiCurriculum.name
          existing
        } else {
          Curriculum.new(apiCurriculum.id) { name = apiCurriculum.name }
        }
      }
    }
  }

  fun scrapeCurricula() {
    transaction {
      Semester.all().forEach { semester -> scrapeCurricula(semester.semesterIdTumOnline) }
    }
  }
}
