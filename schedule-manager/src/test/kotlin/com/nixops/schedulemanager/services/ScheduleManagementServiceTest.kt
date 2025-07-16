package com.nixops.schedulemanager.services

import com.nixops.openapi.scraper.api.DefaultApi
import com.nixops.openapi.scraper.infrastructure.ClientException
import com.nixops.openapi.scraper.model.Appointment as AppointmentDto
import com.nixops.openapi.scraper.model.Course as CourseDto
import com.nixops.openapi.scraper.model.Module as ModuleDto
import com.nixops.schedulemanager.metrics.ScheduleMetrics
import com.nixops.schedulemanager.model.Module
import io.mockk.*
import java.time.LocalDate
import kotlin.test.*
import kotlin.test.Test
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ScheduleManagementServiceTest {

  private lateinit var scraperApiClient: DefaultApi
  private lateinit var scheduleMetrics: ScheduleMetrics
  private lateinit var service: ScheduleManagementService

  private val cacheExpirySeconds = 1L // short for expiry test

  @BeforeEach
  fun setup() {
    scraperApiClient = mockk()
    scheduleMetrics = mockk(relaxed = true)
    service = ScheduleManagementService(scraperApiClient, scheduleMetrics, cacheExpirySeconds)
  }

  private fun stubModuleApi(code: String = "CS101"): ModuleDto {
    val moduleDto = ModuleDto(id = 1, code = code)
    val appointmentDto =
        AppointmentDto(
            appointmentId = 1,
            seriesBeginDate = LocalDate.of(2025, 4, 1),
            seriesEndDate = LocalDate.of(2025, 7, 1),
            beginTime = "10:00",
            endTime = "12:00",
            weekdays = mutableListOf(AppointmentDto.Weekdays.Mo))
    val courseDto =
        CourseDto(courseType = "Vorlesung", appointments = mutableListOf(appointmentDto))
    every { scraperApiClient.getModuleByCode(code) } returns moduleDto
    every { scraperApiClient.getCoursesByModuleCode(code, any()) } returns mutableListOf(courseDto)
    return moduleDto
  }

  @Test
  fun `getOrCreateSchedule creates new schedule if not present`() {
    val schedule = service.getOrCreateSchedule("s1", "2025S")
    assertEquals("s1", schedule.scheduleId)
    assertEquals("2025S", schedule.semester)
    verify { scheduleMetrics.recordScheduleCreated("s1", "2025S") }
  }

  @Test
  fun `getOrCreateSchedule reuses existing schedule`() {
    val created = service.getOrCreateSchedule("s1", "2025S")
    val reused = service.getOrCreateSchedule("s1", "2025S")
    assertEquals(created, reused)
    verify(exactly = 1) { scheduleMetrics.recordScheduleCreated("s1", "2025S") }
  }

  @Test
  fun `getOrCreateSchedule resets modules on semester change`() {
    val initial = service.getOrCreateSchedule("s1", "2024W")
    initial.modules.add(Module(ModuleDto(1, "CS101"), emptyList()))

    val updated = service.getOrCreateSchedule("s1", "2025S")
    assertEquals("2025S", updated.semester)
    assertTrue(updated.modules.isEmpty())
    verify(exactly = 2) { scheduleMetrics.recordScheduleCreated("s1", any()) }
  }

  @Test
  fun `addModule adds module if not already present`() {
    stubModuleApi("CS101")
    service.addModule("s1", "CS101", "2025S")

    val schedule = service.getSchedule("s1")
    assertNotNull(schedule)
    assertEquals(1, schedule.modules.size)
    assertEquals("CS101", schedule.modules.first().module.code)
    verify { scheduleMetrics.recordModuleAdded("s1", "CS101") }
  }

  @Test
  fun `addModule does not add duplicate module`() {
    stubModuleApi("CS101")
    service.addModule("s1", "CS101", "2025S")
    service.addModule("s1", "CS101", "2025S")

    val schedule = service.getSchedule("s1")
    assertEquals(1, schedule?.modules?.size)
    verify(exactly = 1) { scheduleMetrics.recordModuleAdded("s1", "CS101") }
  }

  @Test
  fun `removeModule removes existing module`() {
    stubModuleApi("CS101")
    service.addModule("s1", "CS101", "2025S")
    service.removeModule("s1", "CS101", "2025S")

    val schedule = service.getSchedule("s1")
    assertTrue(schedule?.modules?.isEmpty() == true)
    verify { scheduleMetrics.recordModuleRemoved("s1", "CS101") }
  }

  @Test
  fun `getModuleCodes returns correct module codes`() {
    stubModuleApi("CS101")
    service.addModule("s1", "CS101", "2025S")

    val codes = service.getModuleCodes("s1", "2025S")
    assertEquals(listOf("CS101"), codes)
  }

  @Test
  fun `fetchModule returns null on API failure`() {
    every { scraperApiClient.getModuleByCode(any()) } throws ClientException("Error", 404, null)
    val result = service.fetchModule("INVALID", "2025S")
    assertNull(result)
  }

  @Test
  fun `schedule should expire`() {
    val scheduleId = "s-expiry"
    service.getOrCreateSchedule(scheduleId, "2025S")
    assertNotNull(service.getSchedule(scheduleId), "Should be cached initially")

    Thread.sleep((cacheExpirySeconds + 1) * 1000)
    val expired = service.getSchedule(scheduleId)
    assertNull(expired, "Schedule should expire after configured timeout")
  }
}
