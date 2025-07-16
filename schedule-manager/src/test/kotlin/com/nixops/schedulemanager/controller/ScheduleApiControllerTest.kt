package com.nixops.schedulemanager.controller

import com.nixops.openapi.schedulemanager.model.Appointment
import com.nixops.openapi.scraper.model.Module as ModuleDto
import com.nixops.schedulemanager.metrics.ScheduleMetrics
import com.nixops.schedulemanager.model.Appointment as InternalAppointment
import com.nixops.schedulemanager.model.Module
import com.nixops.schedulemanager.services.ScheduleManagementService
import io.mockk.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ScheduleApiControllerTest {

  private lateinit var service: ScheduleManagementService
  private lateinit var metrics: ScheduleMetrics
  private lateinit var controller: ScheduleApiController

  @BeforeEach
  fun setup() {
    service = mockk()
    metrics = mockk(relaxed = true)
    controller = ScheduleApiController(service, metrics)
  }

  @Test
  fun `addModule should trim input and delegate to service`() {
    every { service.addModule("s1", "CS101", "2025S") } just Runs

    val response = controller.addModule("s1", "2025S", "  \"CS101\"  ")

    assertEquals(200, response.statusCode.value())
    verify { service.addModule("s1", "CS101", "2025S") }
  }

  @Test
  fun `removeModule should trim input and delegate to service`() {
    every { service.removeModule("s1", "CS101", "2025S") } just Runs

    val response = controller.removeModule("s1", "2025S", "\"CS101\"")

    assertEquals(200, response.statusCode.value())
    verify { service.removeModule("s1", "CS101", "2025S") }
  }

  @Test
  fun `getModules should return module codes from service`() {
    every { service.getModuleCodes("s1", "2025S") } returns listOf("CS101", "CS102")

    val response = controller.getModules("s1", "2025S")

    assertEquals(200, response.statusCode.value())
    assertEquals(listOf("CS101", "CS102"), response.body)
  }

  @Test
  fun `getAppointments should return transformed appointments`() {
    val appointment =
        InternalAppointment(
            type = "lecture",
            appointment =
                com.nixops.openapi.scraper.model.Appointment(
                    seriesBeginDate = java.time.LocalDate.of(2025, 4, 1),
                    seriesEndDate = java.time.LocalDate.of(2025, 7, 1),
                    beginTime = "10:00",
                    endTime = "12:00",
                    weekdays =
                        mutableListOf(com.nixops.openapi.scraper.model.Appointment.Weekdays.Mo)))

    val module =
        Module(module = ModuleDto(1, "CS101", "Intro to CS"), appointments = listOf(appointment))

    every { service.getOrCreateSchedule("s1", "2025S") } returns
        com.nixops.schedulemanager.model.Schedule("s1", "2025S", mutableSetOf(module))

    every { metrics.recordAppointmentGeneration(any(), any<() -> Unit>()) } answers
        {
          secondArg<() -> Unit>().invoke()
        }
    every { metrics.recordAppointmentCount(any(), any()) } just Runs

    val response = controller.getAppointments("s1", "2025S")

    assertEquals(200, response.statusCode.value())
    val result = response.body ?: emptyList()

    assertEquals(1, result.size)
    val appt = result[0]
    assertEquals("CS101", appt.moduleCode)
    assertEquals(Appointment.AppointmentType.lecture, appt.appointmentType)
    assertTrue(appt.weekdays!!.contains(Appointment.Weekdays.Mo))
  }

  @Test
  fun `getAppointments should skip appointment with unknown type`() {
    val appointment =
        InternalAppointment(
            type = "Vorlesung",
            appointment =
                com.nixops.openapi.scraper.model.Appointment(
                    seriesBeginDate = java.time.LocalDate.of(2025, 4, 1),
                    seriesEndDate = java.time.LocalDate.of(2025, 7, 1),
                    beginTime = "10:00",
                    endTime = "12:00",
                    weekdays =
                        mutableListOf(com.nixops.openapi.scraper.model.Appointment.Weekdays.Mo)))

    val module =
        Module(module = ModuleDto(1, "CS101", "Intro to CS"), appointments = listOf(appointment))

    every { service.getOrCreateSchedule("s1", "2025S") } returns
        com.nixops.schedulemanager.model.Schedule("s1", "2025S", mutableSetOf(module))
    every { metrics.recordAppointmentGeneration(any(), any<() -> Unit>()) } answers
        {
          secondArg<() -> Unit>().invoke()
        }
    every { metrics.recordAppointmentCount(any(), any()) } just Runs

    val response = controller.getAppointments("s1", "2025S")

    assertEquals(200, response.statusCode.value())
    assertTrue(response.body!!.isEmpty())
  }
}
