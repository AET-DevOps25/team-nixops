package com.nixops.scraper.controller

import com.nixops.openapi.model.Course
import com.nixops.openapi.model.Module
import com.nixops.scraper.mapper.CourseMapper
import com.nixops.scraper.mapper.ModuleMapper
import com.nixops.scraper.model.Course as DomainCourse
import com.nixops.scraper.model.Module as DomainModule
import com.nixops.scraper.model.Semester
import com.nixops.scraper.services.CourseService
import com.nixops.scraper.services.ModuleService
import com.nixops.scraper.services.SemesterService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ModuleApiControllerTest {

  private val moduleService = mockk<ModuleService>()
  private val courseService = mockk<CourseService>()
  private val semesterService = mockk<SemesterService>()
  private val moduleMapper = mockk<ModuleMapper>()
  private val courseMapper = mockk<CourseMapper>()

  private lateinit var controller: ModuleApiController

  @BeforeEach
  fun setUp() {
    controller =
        ModuleApiController(
            moduleService, courseService, semesterService, moduleMapper, courseMapper)
  }

  @Test
  fun `getModules returns 200 with mapped modules when service returns list`() {
    val studyId = 1L
    val semesterKey = "2025S"

    val domainModules = listOf(mockk<DomainModule>(), mockk())
    val apiModules = listOf(mockk<Module>(), mockk())

    every { moduleService.getModules(studyId, semesterKey) } returns domainModules
    every { domainModules[0].let { moduleMapper.moduleToApiModule(it) } } returns apiModules[0]
    every { domainModules[1].let { moduleMapper.moduleToApiModule(it) } } returns apiModules[1]

    val response = controller.getModules(studyId, semesterKey)

    assertEquals(HttpStatus.OK, response.statusCode)
    assertEquals(apiModules, response.body)
    verify(exactly = 1) { moduleService.getModules(studyId, semesterKey) }
    verify(exactly = 1) { moduleMapper.moduleToApiModule(domainModules[0]) }
    verify(exactly = 1) { moduleMapper.moduleToApiModule(domainModules[1]) }
  }

  @Test
  fun `getModules returns 404 when service returns null`() {
    val studyId = 1L
    val semesterKey = "2025S"

    every { moduleService.getModules(studyId, semesterKey) } returns null

    val response = controller.getModules(studyId, semesterKey)

    assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    assertNull(response.body)
    verify(exactly = 1) { moduleService.getModules(studyId, semesterKey) }
  }

  @Test
  fun `getModuleByCode returns 200 with mapped module when found`() {
    val moduleCode = "MOD123"
    val domainModule = mockk<DomainModule>()
    val apiModule = mockk<Module>()

    every { moduleService.getModule(moduleCode) } returns domainModule
    every { moduleMapper.moduleToApiModule(domainModule) } returns apiModule

    val response = controller.getModuleByCode(moduleCode)

    assertEquals(HttpStatus.OK, response.statusCode)
    assertEquals(apiModule, response.body)
    verify(exactly = 1) { moduleService.getModule(moduleCode) }
    verify(exactly = 1) { moduleMapper.moduleToApiModule(domainModule) }
  }

  @Test
  fun `getModuleByCode returns 404 when module not found`() {
    val moduleCode = "MOD123"

    every { moduleService.getModule(moduleCode) } returns null

    val response = controller.getModuleByCode(moduleCode)

    assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    assertNull(response.body)
    verify(exactly = 1) { moduleService.getModule(moduleCode) }
  }

  @Test
  fun `getCoursesByModuleCode returns 200 with mapped courses when all services return data`() {
    val moduleCode = "MOD123"
    val semesterKey = "2025S"
    val domainModule = mockk<DomainModule>()
    val semester = mockk<Semester>()
    val domainCourses = listOf(mockk<DomainCourse>(), mockk())
    val apiCourses = listOf(mockk<Course>(), mockk<Course>())

    every { moduleService.getModule(moduleCode) } returns domainModule
    every { semesterService.getSemester(semesterKey) } returns semester
    every { courseService.getCourses(domainModule, semester) } returns domainCourses.toSet()
    every { courseMapper.courseToApiCourse(domainCourses[0]) } returns apiCourses[0]
    every { courseMapper.courseToApiCourse(domainCourses[1]) } returns apiCourses[1]

    val response = controller.getCoursesByModuleCode(moduleCode, semesterKey)

    assertEquals(HttpStatus.OK, response.statusCode)
    assertEquals(apiCourses, response.body)
    verify(exactly = 1) { moduleService.getModule(moduleCode) }
    verify(exactly = 1) { semesterService.getSemester(semesterKey) }
    verify(exactly = 1) { courseService.getCourses(domainModule, semester) }
    verify(exactly = 1) { courseMapper.courseToApiCourse(domainCourses[0]) }
    verify(exactly = 1) { courseMapper.courseToApiCourse(domainCourses[1]) }
  }

  @Test
  fun `getCoursesByModuleCode returns 404 when module not found`() {
    val moduleCode = "MOD123"
    val semesterKey = "2025S"

    every { moduleService.getModule(moduleCode) } returns null

    val response = controller.getCoursesByModuleCode(moduleCode, semesterKey)

    assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    assertNull(response.body)
    verify(exactly = 1) { moduleService.getModule(moduleCode) }
    verify(exactly = 0) { semesterService.getSemester(any()) }
  }

  @Test
  fun `getCoursesByModuleCode returns 404 when semester not found`() {
    val moduleCode = "MOD123"
    val semesterKey = "2025S"
    val domainModule = mockk<DomainModule>()

    every { moduleService.getModule(moduleCode) } returns domainModule
    every { semesterService.getSemester(semesterKey) } returns null

    val response = controller.getCoursesByModuleCode(moduleCode, semesterKey)

    assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    assertNull(response.body)
    verify(exactly = 1) { moduleService.getModule(moduleCode) }
    verify(exactly = 1) { semesterService.getSemester(semesterKey) }
  }
}
