package com.nixops.scraper.controller

import com.nixops.scraper.model.Semester as DomainSemester
import com.nixops.scraper.services.SemesterService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jetbrains.exposed.dao.id.EntityID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class SemestersApiControllerTest {

  private val semesterService = mockk<SemesterService>()
  private lateinit var controller: SemestersApiController

  @BeforeEach
  fun setup() {
    controller = SemestersApiController(semesterService)
  }

  @Test
  fun `getSemesters returns list of mapped semesters`() {
    val domainSemester1 = mockk<DomainSemester>()
    val domainSemester2 = mockk<DomainSemester>()

    every { domainSemester1.id } returns EntityID("2025S", DomainSemester.table)
    every { domainSemester1.semesterTitle } returns "Spring 2025"
    every { domainSemester1.semesterTag } returns "2025S-tag"

    every { domainSemester2.id } returns EntityID("2025F", DomainSemester.table)
    every { domainSemester2.semesterTitle } returns "Fall 2025"
    every { domainSemester2.semesterTag } returns "2025F-tag"

    every { semesterService.getSemesters() } returns listOf(domainSemester1, domainSemester2)

    val response = controller.getSemesters()

    assertEquals(HttpStatus.OK, response.statusCode)
    val body = response.body
    assertNotNull(body)
    assertEquals(2, body!!.size)
    assertEquals("2025S", body[0].semesterKey)
    assertEquals("Spring 2025", body[0].semesterTitle)
    assertEquals("2025S-tag", body[0].semesterTag)

    assertEquals("2025F", body[1].semesterKey)
    assertEquals("Fall 2025", body[1].semesterTitle)
    assertEquals("2025F-tag", body[1].semesterTag)

    verify(exactly = 1) { semesterService.getSemesters() }
  }

  @Test
  fun `getCurrentSemester returns mapped semester when found`() {
    val domainSemester = mockk<DomainSemester>()
    every { domainSemester.id } returns EntityID("currentSem", DomainSemester.table)
    every { domainSemester.semesterTitle } returns "Current Semester"
    every { domainSemester.semesterTag } returns "current-tag"

    every { semesterService.getSemester("lecture") } returns domainSemester

    val response = controller.getCurrentSemester()

    assertEquals(HttpStatus.OK, response.statusCode)
    val body = response.body
    assertNotNull(body)
    assertEquals("currentSem", body!!.semesterKey)
    assertEquals("Current Semester", body.semesterTitle)
    assertEquals("current-tag", body.semesterTag)

    verify(exactly = 1) { semesterService.getSemester("lecture") }
  }

  @Test
  fun `getCurrentSemester returns 404 when semester not found`() {
    every { semesterService.getSemester("lecture") } returns null

    val response = controller.getCurrentSemester()

    assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    assertNull(response.body)

    verify(exactly = 1) { semesterService.getSemester("lecture") }
  }
}
