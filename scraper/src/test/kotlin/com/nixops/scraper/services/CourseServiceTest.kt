package com.nixops.scraper.services

import com.nixops.scraper.model.*
import com.nixops.scraper.services.scraper.CourseScraper
import io.mockk.*
import java.sql.Connection
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CourseServiceTest {

  private val curriculumService = mockk<CurriculumService>()
  private val courseScraper = mockk<CourseScraper>()

  private lateinit var courseService: CourseService

  @BeforeAll
  fun setupDatabase() {
    Database.connect("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction { create(Courses, CurriculumCourses) }
  }

  @AfterAll
  fun teardownDatabase() {
    transaction { drop(CurriculumCourses, Courses) }
  }

  @BeforeEach
  fun setupService() {
    courseService = CourseService(curriculumService, courseScraper)
  }

  @Test
  fun `getCourse returns course from database if present`() {
    val courseId = 100

    val insertedCourse = transaction { Course.new(courseId) { courseName = "Intro to Testing" } }

    val result = courseService.getCourse(courseId)

    Assertions.assertNotNull(result)
    Assertions.assertEquals("Intro to Testing", result?.courseName)
  }

  @Test
  fun `getCourse scrapes course if not in database`() {
    val courseId = 200

    every { courseScraper.scrapeCourse(courseId) } returns mockk<Course>()

    val result = courseService.getCourse(courseId)

    Assertions.assertNotNull(result)
    verify { courseScraper.scrapeCourse(courseId) }
  }

  @Test
  fun `getCourses returns cached curriculum courses from DB`() {
    // Setup fake curriculum
    val studyProgram = mockk<StudyProgram>()
    val semester = mockk<Semester>()
    val curriculum = mockk<Curriculum>()

    every { curriculum.id.value } returns 10
    every { semester.semesterIdTumOnline } returns 9999
    every { curriculumService.getCurriculum(studyProgram, semester) } returns curriculum

    val course1 = transaction { Course.new(300) { courseName = "Algorithms" } }
    val course2 = transaction { Course.new(301) { courseName = "Data Structures" } }

    transaction {
      CurriculumCourses.insert {
        it[CurriculumCourses.curriculum] = 10
        it[CurriculumCourses.semester] = 9999
        it[CurriculumCourses.course] = 300
      }
      CurriculumCourses.insert {
        it[CurriculumCourses.curriculum] = 10
        it[CurriculumCourses.semester] = 9999
        it[CurriculumCourses.course] = 301
      }
    }

    val result = courseService.getCourses(studyProgram, semester)

    Assertions.assertEquals(2, result.size)
    Assertions.assertTrue(result.any { it.courseName == "Algorithms" })
    Assertions.assertTrue(result.any { it.courseName == "Data Structures" })
  }

  @Test
  fun `getCourses calls scraper and inserts if DB has no curriculum courses`() {
    val studyProgram = mockk<StudyProgram>()
    val semester = mockk<Semester>()
    val curriculum = mockk<Curriculum>()

    every { curriculum.id.value } returns 20
    every { semester.semesterIdTumOnline } returns 8888
    every { curriculumService.getCurriculum(studyProgram, semester) } returns curriculum

    val scrapedCourse = transaction { Course.new(400) { courseName = "Scraped Course" } }

    every { courseScraper.scrapeCourses(20, 8888) } returns setOf(scrapedCourse)

    val result = courseService.getCourses(studyProgram, semester)

    Assertions.assertEquals(1, result.size)
    Assertions.assertEquals(transaction { result.first().courseName }, "Scraped Course")

    // Ensure course got inserted into CurriculumCourses
    val rows = transaction {
      CurriculumCourses.selectAll()
          .where {
            (CurriculumCourses.curriculum eq 20) and
                (CurriculumCourses.semester eq 8888) and
                (CurriculumCourses.course eq 400)
          }
          .count()
    }

    Assertions.assertEquals(1, rows)
  }
}
