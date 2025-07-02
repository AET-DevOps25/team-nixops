package com.nixops.scraper.services.embedding

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.nixops.scraper.mapper.StudyProgramMapper
import com.nixops.scraper.model.*
import com.nixops.scraper.services.CourseService
import com.nixops.scraper.services.ModuleService
import com.nixops.scraper.services.SemesterService
import com.nixops.scraper.services.StudyProgramService
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class EmbeddingScheduler(
    private val studyProgramService: StudyProgramService,
    private val semesterService: SemesterService,
    private val moduleService: ModuleService,
    private val courseService: CourseService,
    //
    private val studyProgramMapper: StudyProgramMapper,
    private val client: OkHttpClient = OkHttpClient()
) {

  @Scheduled(fixedRate = 2 * 1000)
  fun embed() {
    val client =
        OkHttpClient.Builder()
            .connectTimeout(0, TimeUnit.MILLISECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .writeTimeout(0, TimeUnit.MILLISECONDS)
            .build()

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

          val modules = moduleService.getModules(studyId, semester) ?: return@transaction

          val semesterModules =
              mapOf(
                  semesterKey to
                      modules.map { module ->
                        val courses = courseService.getCourses(module, semester)
                        Pair(module, courses)
                      })

          val apiStudyProgram =
              studyProgramMapper.studyProgramToApiStudyProgram(studyProgram, semesterModules)
          val mapper = ObjectMapper()
          mapper
              .registerModule(JavaTimeModule())
              .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

          val jsonString = mapper.writeValueAsString(apiStudyProgram)

          val url = " http://localhost:8000/embed"

          val body = jsonString.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
          val request = Request.Builder().url(url).post(body).build()

          val response = client.newCall(request).execute()

          if (response.code == 404) return@transaction

          if (!response.isSuccessful) {
            throw IOException("Unexpected response: $response")
          }

          val returnBody = response.body?.string()

          println("result: $returnBody")

          StudyProgramSemester.insertIgnore {
            it[StudyProgramSemester.studyProgram] = studyId
            it[StudyProgramSemester.semester] = semesterKey
            it[last_checked] = LocalDateTime.now()
            it[last_embedded] = LocalDateTime.now()
          }
        }
      }
    }
  }
}
