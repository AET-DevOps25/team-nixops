package com.nixops.scraper.services

import com.nixops.scraper.model.StudyProgram
import com.nixops.scraper.model.StudyPrograms
import java.sql.Connection
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StudyProgramServiceTest {

  private lateinit var service: StudyProgramService

  @BeforeAll
  fun setupDatabase() {
    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction { create(StudyPrograms) }
  }

  @AfterAll
  fun teardownDatabase() {
    transaction { drop(StudyPrograms) }
  }

  @BeforeEach
  fun setup() {
    service = StudyProgramService()
    transaction { StudyPrograms.deleteAll() }
  }

  @Test
  fun `getStudyProgram returns program by studyId`() {
    val studyId = 42L
    transaction {
      StudyProgram.new {
        this.studyId = studyId
        this.orgId = 1
        this.spoVersion = "1"
        this.programName = "Test Program"
        this.degreeProgramName = "Computer Science"
        this.degreeTypeName = "Bachelor"
        this.fullName = "Bachelor of Computer Science"
      }
    }

    val result = service.getStudyProgram(studyId.toLong())

    Assertions.assertNotNull(result)
    Assertions.assertEquals(studyId, result?.studyId)
    Assertions.assertEquals("Test Program", result?.programName)
  }

  @Test
  fun `getStudyProgram returns null if none found`() {
    val result = service.getStudyProgram(999L)
    Assertions.assertNull(result)
  }

  @Test
  fun `getStudyPrograms returns latest version per studyId`() {
    transaction {
      StudyProgram.new {
        studyId = 1L
        orgId = 1
        spoVersion = "1"
        programName = "Program 1"
        degreeProgramName = "Degree 1"
        degreeTypeName = "Bachelor"
        fullName = "Full Name 1"
      }
      StudyProgram.new {
        studyId = 1L
        orgId = 1
        spoVersion = "3"
        programName = "Program 3"
        degreeProgramName = "Degree 3"
        degreeTypeName = "Master"
        fullName = "Full Name 3"
      }
      StudyProgram.new {
        studyId = 1L
        orgId = 1
        spoVersion = "2"
        programName = "Program 2"
        degreeProgramName = "Degree 2"
        degreeTypeName = "Bachelor"
        fullName = "Full Name 2"
      }
      StudyProgram.new {
        studyId = 2L
        orgId = 2
        spoVersion = "5"
        programName = "Program 5"
        degreeProgramName = "Degree 5"
        degreeTypeName = "PhD"
        fullName = "Full Name 5"
      }
      StudyProgram.new {
        studyId = 2L
        orgId = 2
        spoVersion = "4"
        programName = "Program 4"
        degreeProgramName = "Degree 4"
        degreeTypeName = "Master"
        fullName = "Full Name 4"
      }
    }

    val results = service.getStudyPrograms()

    Assertions.assertEquals(2, results.size)
    val sp1 = results.find { it.studyId == 1L }
    val sp2 = results.find { it.studyId == 2L }

    Assertions.assertEquals("3", sp1?.spoVersion)
    Assertions.assertEquals("5", sp2?.spoVersion)
  }
}
