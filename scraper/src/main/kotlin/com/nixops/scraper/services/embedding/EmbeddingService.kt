package com.nixops.scraper.services.embedding

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
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
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class EmbeddingService(
    private val moduleService: ModuleService,
    private val courseService: CourseService,
    private val studyProgramMapper: StudyProgramMapper,
    private val client: OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build(),
    embeddingProperties: EmbeddingProperties
) {
  private val endpoint: String = embeddingProperties.endpoint

  fun embed(studyProgram: StudyProgram, semester: Semester) {
    val modules = transaction { moduleService.getModules(studyProgram.studyId, semester) } ?: return

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

    logger.info("Created StudyProgram DTO")

    val body = jsonString.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    val request = Request.Builder().url(endpoint).post(body).build()

    logger.info("Embedding")

    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        throw IOException("Unexpected response: ${response.code} - ${response.message}")
      }
    }

    logger.info("Wait for Embedding")

    waitForEmbedding(studyProgram.studyId, semester.id.value)

    logger.info("Finished Embedding")

    transaction {
      StudyProgramSemester.insertIgnore {
        it[StudyProgramSemester.studyProgram] = studyProgram.studyId
        it[StudyProgramSemester.semester] = semester.id.value
        it[last_checked] = LocalDateTime.now()
        it[last_embedded] = LocalDateTime.now()
      }
    }
  }

  fun waitForEmbedding(
      studyProgramId: Long,
      semester: String,
      timeoutSeconds: Long = 36000,
      pollIntervalMillis: Long = 10000
  ): EmbeddingStudyProgram? {
    val start = System.currentTimeMillis()
    val timeoutMillis = timeoutSeconds * 1000

    while (System.currentTimeMillis() - start < timeoutMillis) {
      val embeddedPrograms = fetchEmbedded()

      logger.trace("waiting for: $studyProgramId, $semester")
      logger.trace("embedded: $embeddedPrograms")

      val found =
          embeddedPrograms.find { it.id == studyProgramId && it.semesters.contains(semester) }
      if (found != null) {
        logger.debug("Study program $studyProgramId with semester $semester has been embedded.")
        return found
      }

      logger.debug(
          "Study program $studyProgramId with semester $semester not embedded yet, waiting...")
      Thread.sleep(pollIntervalMillis)
    }

    logger.warn(
        "Timeout reached: Study program $studyProgramId with semester $semester did not finish embedding within $timeoutSeconds seconds.")
    return null
  }

  fun fetchEmbedded(): List<EmbeddingStudyProgram> {
    val request = Request.Builder().url("http://localhost:8000/embed/studyPrograms").build()

    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        throw IOException("Failed to fetch study programs: ${response.code} - ${response.message}")
      }

      val responseBody = response.body?.string() ?: throw IOException("Empty response body")

      val mapper = ObjectMapper()
      return mapper.readValue(responseBody)
    }
  }
}

data class EmbeddingStudyProgram(
    @JsonProperty("title") val title: String,
    @JsonProperty("id") val id: Long,
    @JsonProperty("semesters") val semesters: List<String>
)
