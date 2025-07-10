package com.nixops.scraper.services.embedding

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.nixops.scraper.config.EmbeddingProperties
import com.nixops.scraper.mapper.StudyProgramMapper
import com.nixops.scraper.model.Semester
import com.nixops.scraper.model.StudyProgram
import com.nixops.scraper.model.StudyProgramSemester
import com.nixops.scraper.services.CourseService
import com.nixops.scraper.services.ModuleService
import java.io.IOException
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class EmbeddingService(
    private val moduleService: ModuleService,
    private val courseService: CourseService,
    private val studyProgramMapper: StudyProgramMapper,
    private val client: OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(4, TimeUnit.HOURS)
            .writeTimeout(4, TimeUnit.HOURS)
            .build(),
    embeddingProperties: EmbeddingProperties
) {
  private val endpoint: String = embeddingProperties.endpoint

  fun embed(studyProgram: StudyProgram, semester: Semester) {
    val modules = moduleService.getModules(studyProgram.studyId, semester) ?: return

    val semesterModules = transaction {
      mapOf(
          semester.id.value to
              modules.map { module ->
                val courses = courseService.getCourses(module, semester)
                Pair(module, courses)
              })
    }

    val apiStudyProgram =
        studyProgramMapper.studyProgramToApiStudyProgram(studyProgram, semesterModules)

    val mapper = ObjectMapper()
    mapper.registerModule(JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    val jsonString = mapper.writeValueAsString(apiStudyProgram)

    val body = jsonString.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    val request = Request.Builder().url(endpoint).post(body).build()

    client.newCall(request).execute().use { response -> // ← use ensures connection closes
      if (!response.isSuccessful) {
        throw IOException("Unexpected response: ${response.code} - ${response.message}")
      }
    }

    transaction {
      StudyProgramSemester.insertIgnore {
        it[StudyProgramSemester.studyProgram] = studyProgram.studyId
        it[StudyProgramSemester.semester] = semester.id.value
        it[last_checked] = LocalDateTime.now()
        it[last_embedded] = LocalDateTime.now()
      }
    }
  }
}
