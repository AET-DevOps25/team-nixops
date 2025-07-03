package com.nixops.scraper.services.scraper

import com.nixops.scraper.model.*
import com.nixops.scraper.tum_api.nat.api.NatProgramApiClient
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class StudyProgramScraper(
    private val studyProgramApiClient: NatProgramApiClient,
) {
  fun scrapeStudyPrograms() {
    val studyPrograms = studyProgramApiClient.getPrograms()
    transaction {
      studyPrograms
          .filter { it.spoVersion != "0" }
          .map {
            logger.debug("Saving study program with name: ${it.degreeProgramName}")

            val existing =
                StudyProgram.find(
                        (StudyPrograms.studyId eq it.studyId) and
                            (StudyPrograms.spoVersion eq it.spoVersion))
                    .firstOrNull()

            if (existing != null) {
              existing.orgId = it.orgId
              existing.spoVersion = it.spoVersion
              existing.programName = it.programName
              existing.degreeProgramName = it.degreeProgramName
              existing.degreeTypeName = it.degree.degreeTypeName
              existing.fullName =
                  "${it.programName} [${it.spoVersion}], ${it.degree.degreeTypeName}"
            } else {
              StudyProgram.new {
                studyId = it.studyId
                orgId = it.orgId
                spoVersion = it.spoVersion
                programName = it.programName
                degreeProgramName = it.degreeProgramName
                degreeTypeName = it.degree.degreeTypeName
                fullName = "${it.programName} [${it.spoVersion}], ${it.degree.degreeTypeName}"
              }
            }
          }
    }
  }
}
