package com.nixops.embeddingbridge.services

import com.nixops.embeddingbridge.EmbeddingCandidate
import com.nixops.embeddingbridge.mapper.StudyProgramMapper
import com.nixops.embeddingbridge.metrics.EmbeddingMetrics
import com.nixops.openapi.genai.api.EmbeddingApi
import com.nixops.openapi.genai.model.StudyProgram
import com.nixops.openapi.genai.model.StudyProgramSelectorItem
import com.nixops.openapi.scraper.api.DefaultApi
import com.nixops.openapi.scraper.model.StudyProgram as ScraperStudyProgram
import io.mockk.*
import java.math.BigDecimal
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EmbeddingBridgeServiceTest {

  private lateinit var embeddingApiClient: EmbeddingApi
  private lateinit var scraperApiClient: DefaultApi
  private lateinit var studyProgramMapper: StudyProgramMapper
  private lateinit var embeddingMetrics: EmbeddingMetrics

  private lateinit var service: EmbeddingBridgeService

  @BeforeEach
  fun setUp() {
    embeddingApiClient = mockk()
    scraperApiClient = mockk()
    studyProgramMapper = mockk()
    embeddingMetrics = mockk()

    service =
        EmbeddingBridgeService(
            embeddingApiClient, scraperApiClient, studyProgramMapper, embeddingMetrics)
  }

  @Test
  fun `fetchEmbeddingCandidates returns candidates based on scraper and embedded genai data`() {
    // Setup scraper data (scraper model)
    val scraperFinishedPrograms =
        mutableListOf(
            ScraperStudyProgram().apply {
              studyId = 1L
              programName = "Program A"
              semesters = mutableMapOf("2021" to mutableListOf())
            },
            ScraperStudyProgram().apply {
              studyId = 2L
              programName = "Program B"
              semesters = mutableMapOf("2021" to mutableListOf())
            })

    // Setup embedded genai study programs
    val genaiEmbeddedPrograms =
        mutableListOf(
            StudyProgramSelectorItem().apply {
              id = BigDecimal.valueOf(1L)
              semesters = mutableListOf("2021")
              title = "Program A"
            })

    every { scraperApiClient.getFinishedStudyPrograms() } returns scraperFinishedPrograms
    every { embeddingApiClient.fetchStudyPrograms() } returns genaiEmbeddedPrograms

    val candidates = service.fetchEmbeddingCandidates()

    // Only Program B with semester 2021 should be a candidate, since Program A is already embedded
    assertEquals(1, candidates.size)
    val candidate = candidates[0]
    assertEquals(2L, candidate.id)
    assertEquals("Program B", candidate.name)
    assertEquals("2021", candidate.semester)
  }

  @Test
  fun `fetchStudyProgram uses scraper client and mapper to return genai StudyProgram`() {
    val candidate = EmbeddingCandidate(1L, "Program A", "2021")

    // scraper returns scraper StudyProgram
    val scraperStudyProgram =
        ScraperStudyProgram().apply {
          studyId = 1L
          programName = "Program A"
          semesters = mutableMapOf("2021" to mutableListOf())
          degreeTypeName = "Bachelor"
        }

    // mapper returns genai StudyProgram
    val genaiStudyProgram =
        StudyProgram().apply {
          studyId = 1L
          programName = "Program A"
          degreeTypeName = "Bachelor"
          semesters = mutableMapOf("2021" to mutableListOf())
        }

    every { scraperApiClient.getFullStudyProgram(candidate.id, candidate.semester) } returns
        scraperStudyProgram
    every { studyProgramMapper.map(scraperStudyProgram) } returns genaiStudyProgram

    val result = service.fetchStudyProgram(candidate)

    assertEquals(genaiStudyProgram, result)
    verify { scraperApiClient.getFullStudyProgram(candidate.id, candidate.semester) }
    verify { studyProgramMapper.map(scraperStudyProgram) }
  }

  @Test
  fun `embedNextCandidate embeds study program using mapper and embeddingApi`() {
    val candidate = EmbeddingCandidate(1L, "Program A", "2021")

    val scraperStudyProgram =
        ScraperStudyProgram().apply {
          studyId = 1L
          programName = "Program A"
          degreeTypeName = "Bachelor"
          semesters = mutableMapOf("2021" to mutableListOf())
        }

    val genaiStudyProgram =
        StudyProgram().apply {
          studyId = 1L
          programName = "Program A"
          degreeTypeName = "Bachelor"
          semesters = mutableMapOf("2021" to mutableListOf())
        }

    // Partial mock service to mock fetchNextEmbeddingCandidate and fetchStudyProgram
    val spykService = spyk(service)

    every { spykService.fetchNextEmbeddingCandidate() } returns candidate
    every { scraperApiClient.getFullStudyProgram(candidate.id, candidate.semester) } returns
        scraperStudyProgram
    every { studyProgramMapper.map(scraperStudyProgram) } returns genaiStudyProgram
    every { spykService.fetchStudyProgram(candidate) } returns genaiStudyProgram
    every { spykService.embed(genaiStudyProgram) } just Runs

    spykService.embedNextCandidate()

    verify { spykService.fetchNextEmbeddingCandidate() }
    verify { spykService.fetchStudyProgram(candidate) }
    verify { spykService.embed(genaiStudyProgram) }
  }
}
