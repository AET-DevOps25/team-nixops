package com.nixops.embedderbridge

import com.nixops.embedderbridge.mapper.StudyProgramMapper
import com.nixops.openapi.genai.api.EmbeddingApi
import com.nixops.openapi.genai.model.StudyProgram as GenAIStudyProgram
import com.nixops.openapi.scraper.api.DefaultApi
import java.net.ConnectException
import java.time.Duration
import java.time.LocalTime
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

data class EmbeddingCandidate(
    val id: Long,
    val name: String,
    val semester: String,
)

@SpringBootApplication
@RestController
class EmbeddingBridgeApplication(
    private val embeddingApiClient: EmbeddingApi,
    private val scraperApiClient: DefaultApi,
    private val studyProgramMapper: StudyProgramMapper
) {

  @GetMapping("/hello", produces = [MediaType.TEXT_PLAIN_VALUE])
  fun hello(): ResponseEntity<String> {
    embedNextCandidate()
    return ResponseEntity.ok("")
  }

  fun fetchEmbeddingCandidates(): List<EmbeddingCandidate> {
    val finishedStudyPrograms = scraperApiClient.getFinishedStudyPrograms()
    val embeddedStudyPrograms = embeddingApiClient.fetchStudyPrograms()

    val embeddingCandidates = mutableListOf<EmbeddingCandidate>()
    for (studyProgram in finishedStudyPrograms) {
      val semesters = studyProgram.semesters ?: continue
      val studyId = studyProgram.studyId ?: continue
      val programName = studyProgram.programName ?: continue

      for (semester in semesters.keys) {
        if (embeddedStudyPrograms.any { esp ->
          esp.id?.toLong() == studyId && esp.semesters?.contains(semester) == true
        }) {
          logger.debug("Already embedded: ${studyId}, ${programName}, $semester")
        } else {

          logger.debug("Not yet embedded: ${studyId}, ${programName}, $semester")

          embeddingCandidates.add(EmbeddingCandidate(studyId, programName, semester))
        }
      }
    }
    return embeddingCandidates
  }

  fun fetchNextEmbeddingCandidate(): EmbeddingCandidate? {
    return fetchEmbeddingCandidates().firstOrNull()
  }

  fun fetchStudyProgram(candidate: EmbeddingCandidate): GenAIStudyProgram {
    val studyProgram = scraperApiClient.getFullStudyProgram(candidate.id, candidate.semester)
    return studyProgramMapper.map(studyProgram)
  }

  fun embedNextCandidate() {
    val candidate =
        try {
          fetchNextEmbeddingCandidate()
        } catch (e: ConnectException) {
          logger.error(e) { "Failed to connect to Scraper" }
          return
        }

    if (candidate != null) {
      logger.info("Embed ${candidate.id}, ${candidate.name}, ${candidate.semester}")
      logger.info("Fetching study program")

      val studyProgram =
          try {
            fetchStudyProgram(candidate)
          } catch (e: ConnectException) {
            logger.error(e) { "Failed to connect to Scraper" }
            return
          }

      logger.info("Embedding study program")

      try {
        embed(studyProgram)
      } catch (e: ConnectException) {
        logger.error(e) { "Failed to connect to GenAI" }
        return
      }
    } else {
      logger.info("No study program to embed")
    }
  }

  fun embed(studyProgram: GenAIStudyProgram) {
    embeddingApiClient.createStudyProgram(studyProgram)
    waitForEmbedding(studyProgram)
  }

  fun checkEmbedding(studyProgram: GenAIStudyProgram): Boolean {
    val semesters = studyProgram.semesters ?: return true
    val studyId = studyProgram.studyId ?: return true

    return embeddingApiClient.fetchStudyPrograms().any { embedded ->
      embedded.id?.toLong() == studyId &&
          semesters.keys.all { semester -> embedded.semesters?.contains(semester) == true }
    }
  }

  fun waitForEmbedding(
      studyProgram: GenAIStudyProgram,
      timeout: Duration = Duration.ofHours(6),
      interval: Duration = Duration.ofSeconds(10)
  ) {
    val start = LocalTime.now()

    while (Duration.between(start, LocalTime.now()) < timeout) {
      if (checkEmbedding(studyProgram)) {

        logger.info("Finished embedding study program")
        return
      }
      Thread.sleep(interval)
    }

    logger.error("Timeout embedding study program")
  }
}

fun main(args: Array<String>) {
  runApplication<EmbeddingBridgeApplication>(*args)
}
