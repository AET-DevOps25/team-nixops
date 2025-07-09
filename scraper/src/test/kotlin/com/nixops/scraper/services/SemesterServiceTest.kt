package com.nixops.scraper.services

import com.nixops.scraper.model.Semester
import com.nixops.scraper.services.scraper.SemesterScraper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SemesterServiceTest {

  private val semesterScraper = mockk<SemesterScraper>()
  private val semesterService = SemesterService(semesterScraper)

  @BeforeEach
  fun setup() {
    // Connect to in-memory H2 DB
    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

    // Create tables
    transaction { SchemaUtils.create(Semester.table) }
  }

  @AfterEach
  fun teardown() {
    transaction { SchemaUtils.drop(Semester.table) }
  }

  @Test
  fun `getSemester should return semester from scraper`() {
    val semesterKey = "2025S"
    val expectedSemester = mockk<Semester>()
    every { semesterScraper.scrapeSemester(semesterKey) } returns expectedSemester

    val result = semesterService.getSemester(semesterKey)

    assertNotNull(result)
    assertEquals(expectedSemester, result)
    verify(exactly = 1) { semesterScraper.scrapeSemester(semesterKey) }
  }

  @Test
  fun `getSemester should return null when scraper returns null`() {
    val semesterKey = "unknown"
    every { semesterScraper.scrapeSemester(semesterKey) } returns null

    val result = semesterService.getSemester(semesterKey)

    assertNull(result)
    verify(exactly = 1) { semesterScraper.scrapeSemester(semesterKey) }
  }

  @Test
  fun `getSemesters should return list of semesters`() {
    transaction {
      Semester.new("2025S") {
        semesterTag = "2025S"
        semesterTitle = "Spring 2025"
        semesterIdTumOnline = 463743
      }
      Semester.new("2025F") {
        semesterTag = "2025F"
        semesterTitle = "Fall 2025"
        semesterIdTumOnline = 343748
      }
    }

    val semesters = semesterService.getSemesters()

    assertNotNull(semesters)
    assertEquals(2, semesters.size)
    assertTrue(semesters.any { it.semesterTag == "2025S" })
    assertTrue(semesters.any { it.semesterTag == "2025F" })
  }
}
