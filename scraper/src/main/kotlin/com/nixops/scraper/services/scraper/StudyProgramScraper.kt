package com.nixops.scraper.services.scraper

import com.nixops.scraper.model.*
import com.nixops.scraper.tum_api.nat.api.NatProgramApiClient
import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
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
          .map { studyProgram ->
            logger.debug("Saving study program with name: ${studyProgram.degreeProgramName}")

            StudyPrograms.upsert(StudyPrograms.studyId, StudyPrograms.spoVersion) {
              it[StudyPrograms.studyId] = studyProgram.studyId
              it[StudyPrograms.orgId] = studyProgram.orgId
              it[StudyPrograms.spoVersion] = studyProgram.spoVersion
              it[StudyPrograms.programName] = studyProgram.programName
              it[StudyPrograms.degreeProgramName] = studyProgram.degreeProgramName
              it[StudyPrograms.degreeTypeName] = studyProgram.degree.degreeTypeName
              it[StudyPrograms.fullName] =
                  "${studyProgram.programName} [${studyProgram.spoVersion}], ${studyProgram.degree.degreeTypeName}"
            }
          }
    }
  }
}
