package com.nixops.scraper.services

import com.nixops.scraper.model.*
import io.mockk.every
import io.mockk.mockk
import java.sql.Connection
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CurriculumServiceTest {

  private lateinit var curriculumService: CurriculumService

  @BeforeAll
  fun setup() {
    // Setup in-memory database
    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction { SchemaUtils.create(Curriculums, StudyPrograms) }

    curriculumService = CurriculumService()
  }

  @AfterAll
  fun tearDown() {
    transaction { SchemaUtils.drop(Curriculums, StudyPrograms) }
  }

  @BeforeEach
  fun clearData() {
    transaction { Curriculums.deleteAll() }
  }

  @Test
  fun `getCurriculum returns curriculum when found by study program full name`() {
    // Setup test data
    val testFullName = "Test Program Full Name"
    transaction {
      Curriculum.new {
        name = testFullName
        // Set other required fields if any
      }
    }

    // Create mock study program
    val mockStudyProgram = mockk<StudyProgram>()
    every { mockStudyProgram.fullName } returns testFullName

    // Create mock semester (not used in current implementation)
    val mockSemester = mockk<Semester>()

    // Test
    val result = curriculumService.getCurriculum(mockStudyProgram, mockSemester)

    // Verify
    assertEquals(testFullName, result?.name)
  }

  @Test
  fun `getCurriculum returns null when no curriculum matches study program full name`() {
    // Setup test data (empty database due to @BeforeEach clear)

    // Create mock study program
    val mockStudyProgram = mockk<StudyProgram>()
    every { mockStudyProgram.fullName } returns "Non-existent Program"

    // Create mock semester
    val mockSemester = mockk<Semester>()

    // Test
    val result = curriculumService.getCurriculum(mockStudyProgram, mockSemester)

    // Verify
    assertNull(result)
  }

  @Test
  fun `getCurriculum handles multiple curricula but returns first match`() {
    // Setup test data
    val testFullName = "Duplicate Program Name"
    transaction {
      Curriculum.new {
        name = testFullName
        // Set other fields
      }
      Curriculum.new {
        name = testFullName
        // Set other fields
      }
      Curriculum.new {
        name = "Other Program"
        // Set other fields
      }
    }

    // Create mock study program
    val mockStudyProgram = mockk<StudyProgram>()
    every { mockStudyProgram.fullName } returns testFullName

    // Create mock semester
    val mockSemester = mockk<Semester>()

    // Test
    val result = curriculumService.getCurriculum(mockStudyProgram, mockSemester)

    // Verify
    assertEquals(testFullName, result?.name)
  }
}
