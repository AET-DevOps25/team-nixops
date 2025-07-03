package com.nixops.scraper.services.embedding

import com.nixops.scraper.model.*
import com.nixops.scraper.services.SemesterService
import com.nixops.scraper.services.StudyProgramService
import java.time.Duration
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class EmbeddingScheduler(
    private val studyProgramService: StudyProgramService,
    private val semesterService: SemesterService,
    private val embeddingService: EmbeddingService,
) {

  @Scheduled(fixedRate = 2 * 1000)
  fun embed() {
    transaction {
      val res =
          CurriculumCourses.join(
                  Curriculums, JoinType.INNER, CurriculumCourses.curriculum, Curriculums.id)
              .join(StudyPrograms, JoinType.INNER, Curriculums.name, StudyPrograms.fullName)
              .join(
                  Semesters,
                  JoinType.INNER,
                  CurriculumCourses.semester,
                  Semesters.semesterIdTumOnline)
              .select(
                  StudyPrograms.programName,
                  StudyPrograms.studyId,
                  Semesters.id,
                  CurriculumCourses.curriculum)
              .withDistinct()

      for (row in res) {
        val name = row[StudyPrograms.programName]
        val studyId = row[StudyPrograms.studyId]
        val semesterKey = row[Semesters.id].value
        val curriculumId = row[CurriculumCourses.curriculum]

        val existingStudyProgramSemester =
            StudyProgramSemester.selectAll()
                .where(
                    (StudyProgramSemester.studyProgram eq studyId) and
                        (StudyProgramSemester.semester eq semesterKey))
                .firstOrNull()

        val now = LocalDateTime.now()

        val embed =
            if (existingStudyProgramSemester != null) {
              val lastEmbedded = existingStudyProgramSemester[StudyProgramSemester.last_embedded]

              Duration.between(lastEmbedded, now) > Duration.ofDays(7)
            } else {
              true
            }

        if (embed) {
          println("embed: $name, $studyId, $semesterKey, $curriculumId")

          val studyProgram = studyProgramService.getStudyProgram(studyId) ?: return@transaction

          val semester = semesterService.getSemester(semesterKey) ?: return@transaction

          embeddingService.embed(studyProgram, semester)
        }
      }
    }
  }
}
