package com.nixops.scraper.controller

import com.nixops.openapi.model.StudyProgram
import com.nixops.scraper.mapper.StudyProgramMapper
import com.nixops.scraper.model.Course as DomainCourse
import com.nixops.scraper.model.Module as DomainModule
import com.nixops.scraper.model.Semester
import com.nixops.scraper.model.StudyProgram as DomainStudyProgram
import com.nixops.scraper.services.CourseService
import com.nixops.scraper.services.ModuleService
import com.nixops.scraper.services.SemesterService
import com.nixops.scraper.services.StudyProgramService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class StudyProgramsApiControllerTest {

  private val studyProgramService = mockk<StudyProgramService>()
  private val semesterService = mockk<SemesterService>()
  private val moduleService = mockk<ModuleService>()
  private val courseService = mockk<CourseService>()
  private val studyProgramMapper = mockk<StudyProgramMapper>()

  private lateinit var controller: StudyProgramsApiController

  @BeforeEach
  fun setUp() {
    controller =
        StudyProgramsApiController(
            studyProgramService, semesterService, moduleService, courseService, studyProgramMapper)
  }

  @Test
  fun `getStudyPrograms returns list of mapped study programs`() {
    val domainStudyPrograms = listOf(mockk<DomainStudyProgram>(), mockk())
    val apiStudyPrograms = listOf(mockk<StudyProgram>(), mockk())

    every { studyProgramService.getStudyPrograms() } returns domainStudyPrograms
    every { studyProgramMapper.studyProgramToApiStudyProgram(domainStudyPrograms[0]) } returns
        apiStudyPrograms[0]
    every { studyProgramMapper.studyProgramToApiStudyProgram(domainStudyPrograms[1]) } returns
        apiStudyPrograms[1]

    val response = controller.getStudyPrograms()

    assertEquals(HttpStatus.OK, response.statusCode)
    assertEquals(apiStudyPrograms, response.body)
    verify(exactly = 1) { studyProgramService.getStudyPrograms() }
    verify(exactly = 1) { studyProgramMapper.studyProgramToApiStudyProgram(domainStudyPrograms[0]) }
    verify(exactly = 1) { studyProgramMapper.studyProgramToApiStudyProgram(domainStudyPrograms[1]) }
  }

  @Test
  fun `getStudyProgram returns 200 with mapped study program when found`() {
    val studyId = 1L
    val domainStudyProgram = mockk<DomainStudyProgram>()
    val apiStudyProgram = mockk<StudyProgram>()

    every { studyProgramService.getStudyProgram(studyId) } returns domainStudyProgram
    every { studyProgramMapper.studyProgramToApiStudyProgram(domainStudyProgram) } returns
        apiStudyProgram

    val response = controller.getStudyProgram(studyId, "2025S")

    assertEquals(HttpStatus.OK, response.statusCode)
    assertEquals(apiStudyProgram, response.body)
    verify(exactly = 1) { studyProgramService.getStudyProgram(studyId) }
    verify(exactly = 1) { studyProgramMapper.studyProgramToApiStudyProgram(domainStudyProgram) }
  }

  @Test
  fun `getStudyProgram returns 404 when study program not found`() {
    val studyId = 1L
    every { studyProgramService.getStudyProgram(studyId) } returns null

    val response = controller.getStudyProgram(studyId, "2025S")

    assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    assertNull(response.body)
    verify(exactly = 1) { studyProgramService.getStudyProgram(studyId) }
  }

  @Test
  fun `getFullStudyProgram returns 200 with mapped study program when all data found`() {
    val studyId = 1L
    val semesterKey = "2025S"

    val domainStudyProgram = mockk<DomainStudyProgram>()
    val semester = mockk<Semester>()
    val domainModules = listOf(mockk<DomainModule>(), mockk())
    val domainCourses1 = listOf(mockk<DomainCourse>())
    val domainCourses2 = listOf<DomainCourse>()
    val apiStudyProgram = mockk<StudyProgram>()

    every { studyProgramService.getStudyProgram(studyId) } returns domainStudyProgram
    every { semesterService.getSemester(semesterKey) } returns semester
    every { moduleService.getModules(studyId, semester) } returns domainModules
    every { courseService.getCourses(domainModules[0], semester) } returns domainCourses1.toSet()
    every { courseService.getCourses(domainModules[1], semester) } returns domainCourses2.toSet()

    every { studyProgramMapper.studyProgramToApiStudyProgram(domainStudyProgram, any()) } returns
        apiStudyProgram

    val response = controller.getFullStudyProgram(studyId, semesterKey)

    assertEquals(HttpStatus.OK, response.statusCode)
    assertEquals(apiStudyProgram, response.body)
    verify(exactly = 1) { studyProgramService.getStudyProgram(studyId) }
    verify(exactly = 1) { semesterService.getSemester(semesterKey) }
    verify(exactly = 1) { moduleService.getModules(studyId, semester) }
    verify(exactly = 1) { courseService.getCourses(domainModules[0], semester) }
    verify(exactly = 1) { courseService.getCourses(domainModules[1], semester) }
    verify(exactly = 1) {
      studyProgramMapper.studyProgramToApiStudyProgram(domainStudyProgram, any())
    }
  }

  @Test
  fun `getFullStudyProgram returns 404 when study program not found`() {
    val studyId = 1L
    val semesterKey = "2025S"

    every { studyProgramService.getStudyProgram(studyId) } returns null

    val response = controller.getFullStudyProgram(studyId, semesterKey)

    assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    assertNull(response.body)
    verify(exactly = 1) { studyProgramService.getStudyProgram(studyId) }
    verify(exactly = 0) { semesterService.getSemester(any()) }
  }

  @Test
  fun `getFullStudyProgram returns 404 when semester not found`() {
    val studyId = 1L
    val semesterKey = "2025S"
    val domainStudyProgram = mockk<DomainStudyProgram>()

    every { studyProgramService.getStudyProgram(studyId) } returns domainStudyProgram
    every { semesterService.getSemester(semesterKey) } returns null

    val response = controller.getFullStudyProgram(studyId, semesterKey)

    assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    assertNull(response.body)
    verify(exactly = 1) { studyProgramService.getStudyProgram(studyId) }
    verify(exactly = 1) { semesterService.getSemester(semesterKey) }
  }

  @Test
  fun `getFullStudyProgram returns 404 when modules not found`() {
    val studyId = 1L
    val semesterKey = "2025S"
    val domainStudyProgram = mockk<DomainStudyProgram>()
    val semester = mockk<Semester>()

    every { studyProgramService.getStudyProgram(studyId) } returns domainStudyProgram
    every { semesterService.getSemester(semesterKey) } returns semester
    every { moduleService.getModules(studyId, semester) } returns null

    val response = controller.getFullStudyProgram(studyId, semesterKey)

    assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    assertNull(response.body)
    verify(exactly = 1) { studyProgramService.getStudyProgram(studyId) }
    verify(exactly = 1) { semesterService.getSemester(semesterKey) }
    verify(exactly = 1) { moduleService.getModules(studyId, semester) }
  }
}
