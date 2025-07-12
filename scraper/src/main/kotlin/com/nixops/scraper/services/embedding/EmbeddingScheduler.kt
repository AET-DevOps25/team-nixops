package com.nixops.scraper.services.embedding

import com.nixops.scraper.model.*
import com.nixops.scraper.services.SemesterService
import com.nixops.scraper.services.StudyProgramService
import java.net.ConnectException
import java.time.Duration
import java.time.LocalDateTime
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

private data class EmbeddingCandidate(
    val studyId: Long,
    val semesterKey: String,
    val programName: String,
    val curriculumId: Int
)

@Component
class EmbeddingScheduler(
    private val studyProgramService: StudyProgramService,
    private val semesterService: SemesterService,
    private val embeddingService: EmbeddingService,
) {

  @Scheduled(fixedRate = 2 * 1000)
  fun embed() {
    val candidates = transaction {
      CurriculumCourses.join(
              Curriculums, JoinType.INNER, CurriculumCourses.curriculum, Curriculums.id)
          .join(StudyPrograms, JoinType.INNER, Curriculums.name, StudyPrograms.fullName)
          .join(
              Semesters, JoinType.INNER, CurriculumCourses.semester, Semesters.semesterIdTumOnline)
          .select(
              StudyPrograms.programName,
              StudyPrograms.studyId,
              Semesters.id,
              CurriculumCourses.curriculum)
          .withDistinct()
          .mapNotNull { row ->
            val studyId = row[StudyPrograms.studyId]
            val semesterKey = row[Semesters.id].value
            EmbeddingCandidate(
                studyId = studyId,
                semesterKey = semesterKey,
                programName = row[StudyPrograms.programName],
                curriculumId = row[CurriculumCourses.curriculum])
          }
    }

    for (candidate in candidates) {
      val name = candidate.programName
      val studyId = candidate.studyId
      val semesterKey = candidate.semesterKey
      val curriculumId = candidate.curriculumId

      val embed = transaction {
        val existingStudyProgramSemester =
            StudyProgramSemester.selectAll()
                .where(
                    (StudyProgramSemester.studyProgram eq studyId) and
                        (StudyProgramSemester.semester eq semesterKey))
                .firstOrNull()

        val now = LocalDateTime.now()

        if (existingStudyProgramSemester != null) {
          val lastEmbedded = existingStudyProgramSemester[StudyProgramSemester.last_embedded]

          Duration.between(lastEmbedded, now) > Duration.ofDays(7)
        } else {
          true
        }
      }

      if (embed) {
        logger.info("Embed $name, $studyId, $semesterKey, $curriculumId")

        val studyProgram = studyProgramService.getStudyProgram(studyId) ?: continue

        val semester = semesterService.getSemester(semesterKey) ?: continue

        try {
          embeddingService.embed(studyProgram, semester)
        } catch (e: ConnectException) {
          logger.error(e) { "Failed to connect to GenAI" }
          break
        } catch (e: Exception) {
          logger.error(e) {
            "Unexpected error while embedding $studyId, $semesterKey, $curriculumId: ${e.message}"
          }
        }
      }
    }
  }
}
