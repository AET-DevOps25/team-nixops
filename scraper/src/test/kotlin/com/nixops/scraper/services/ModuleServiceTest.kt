package com.nixops.scraper.services

import com.nixops.scraper.model.*
import com.nixops.scraper.services.scraper.ModuleScraper
import io.mockk.*
import java.sql.Connection
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModuleServiceTest {

  private val courseService = mockk<CourseService>()
  private val semesterService = mockk<SemesterService>()
  private val moduleScraper = mockk<ModuleScraper>()
  private lateinit var moduleService: ModuleService

  @BeforeAll
  fun setupDatabase() {
    Database.connect("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    transaction { create(Modules, Courses, ModuleCourses, StudyPrograms) }
  }

  @AfterAll
  fun tearDownDatabase() {
    transaction { drop(ModuleCourses, Modules, Courses, StudyPrograms) }
  }

  @BeforeEach
  fun setupService() {
    moduleService = ModuleService(courseService, semesterService, moduleScraper)
  }

  @Test
  fun `getModule returns module from DB if it exists`() {
    val code = "IN001"
    transaction {
      Module.new(1) {
        moduleCode = code
        moduleTitle = "Intro to CS"
        moduleCredits = 5.0F
      }
    }

    val result = moduleService.getModule(code)
    Assertions.assertNotNull(result)
    Assertions.assertEquals("Intro to CS", result?.moduleTitle)
  }

  @Test
  fun `getModule scrapes module if not found in DB`() {
    val code = "IN999"
    val scrapedModule = mockk<Module>()

    every { moduleScraper.scrapeModuleByCode(code) } returns scrapedModule

    val result = moduleService.getModule(code)

    Assertions.assertEquals(scrapedModule, result)
    verify { moduleScraper.scrapeModuleByCode(code) }
  }

  @Test
  fun `getModuleIds returns module IDs linked to courses for given study program and semester`() {
    val semester = mockk<Semester>()
    val studyProgram = mockk<StudyProgram>()

    every { semester.id.value } returns "123"
    val course1 = transaction { Course.new(1) { courseName = "Course 1" } }
    val course2 = transaction { Course.new(2) { courseName = "Course 2" } }

    every { courseService.getCourses(studyProgram, semester) } returns setOf(course1, course2)

    transaction {
      Module.new(10) {
        moduleCode = "MOD101"
        moduleTitle = "Module 1"
        moduleCredits = 5.0F
      }
      Module.new(11) {
        moduleCode = "MOD102"
        moduleTitle = "Module 2"
        moduleCredits = 3.5F
      }

      ModuleCourses.insert {
        it[module] = 10
        it[course] = 1
        it[this.semester] = "123"
      }
      ModuleCourses.insert {
        it[module] = 11
        it[course] = 2
        it[this.semester] = "123"
      }
    }

    val result = moduleService.getModuleIds(studyProgram, semester)

    Assertions.assertEquals(setOf(10, 11), result)
  }

  @Test
  fun `getModuleIds returns null if no study programs are found`() {
    val semester = mockk<Semester>()
    every { semester.id.value } returns "456"

    val studyId = 999L

    val result = moduleService.getModuleIds(studyId, semester)

    Assertions.assertNull(result)
  }
}
