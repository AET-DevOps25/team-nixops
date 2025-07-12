package com.nixops.embeddingbridge.services

import com.nixops.embeddingbridge.EmbeddingCandidate
import com.nixops.embeddingbridge.mapper.StudyProgramMapper
import com.nixops.embeddingbridge.metrics.EmbeddingMetrics
import com.nixops.openapi.genai.api.EmbeddingApi
import com.nixops.openapi.genai.model.StudyProgram
import com.nixops.openapi.scraper.api.DefaultApi
import java.net.ConnectException
import java.time.Duration
import java.time.LocalTime
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class EmbeddingBridgeService(
    private val embeddingApiClient: EmbeddingApi,
    private val scraperApiClient: DefaultApi,
    private val studyProgramMapper: StudyProgramMapper,
    //
    private val embeddingMetrics: EmbeddingMetrics
) {
  fun fetchEmbeddingCandidates(): List<EmbeddingCandidate> {
    val finishedStudyPrograms =
        try {
          scraperApiClient.getFinishedStudyPrograms()
        } catch (e: ConnectException) {
          logger.error(e) { "Failed to connect to Scraper" }
          return listOf()
        }

    val embeddedStudyPrograms =
        try {
          embeddingApiClient.fetchStudyPrograms()
        } catch (e: ConnectException) {
          logger.error(e) { "Failed to connect to GenAI" }
          return listOf()
        }

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
    return fetchEmbeddingCandidates().randomOrNull()
  }

  fun fetchStudyProgram(candidate: EmbeddingCandidate): StudyProgram {
    val studyProgram = scraperApiClient.getFullStudyProgram(candidate.id, candidate.semester)
    return studyProgramMapper.map(studyProgram)
  }

  fun embedNextCandidate() {
    val candidate = fetchNextEmbeddingCandidate()

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

  fun embed(studyProgram: StudyProgram) {
    val studyId = studyProgram.studyId ?: return
    val programName = studyProgram.programName ?: return
    val degreeTypeName = studyProgram.degreeTypeName ?: return

    embeddingMetrics.recordEmbedding(studyId, programName, degreeTypeName) {
      embeddingApiClient.createStudyProgram(studyProgram)
      waitForEmbedding(studyProgram)
    }
  }

  fun checkEmbedding(studyProgram: StudyProgram): Boolean {
    val semesters = studyProgram.semesters ?: return true
    val studyId = studyProgram.studyId ?: return true

    return embeddingApiClient.fetchStudyPrograms().any { embedded ->
      embedded.id?.toLong() == studyId &&
          semesters.keys.all { semester -> embedded.semesters?.contains(semester) == true }
    }
  }

  fun waitForEmbedding(
      studyProgram: StudyProgram,
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
