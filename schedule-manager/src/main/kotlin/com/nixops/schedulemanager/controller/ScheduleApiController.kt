package com.nixops.schedulemanager.controller

import com.nixops.openapi.schedulemanager.api.ScheduleApi
import com.nixops.openapi.schedulemanager.model.Appointment
import com.nixops.schedulemanager.metrics.ScheduleMetrics
import com.nixops.schedulemanager.services.ScheduleManagementService
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

private val logger = KotlinLogging.logger {}

@Controller
class ScheduleApiController(
    private val scheduleManagementService: ScheduleManagementService,
    //
    private val scheduleMetrics: ScheduleMetrics,
) : ScheduleApi {
  override fun addModule(scheduleId: String, semester: String, body: String): ResponseEntity<Unit> {
    val trimmedModuleCode = body.trim().trim('"')
    scheduleManagementService.addModule(scheduleId, trimmedModuleCode, semester)
    return ResponseEntity.ok().build()
  }

  override fun removeModule(
      scheduleId: String,
      semester: String,
      body: String
  ): ResponseEntity<Unit> {
    val trimmedModuleCode = body.trim().trim('"')
    scheduleManagementService.removeModule(scheduleId, trimmedModuleCode, semester)
    return ResponseEntity.ok().build()
  }

  override fun getModules(scheduleId: String, semester: String): ResponseEntity<List<String>> {
    val schedule = scheduleManagementService.getModuleCodes(scheduleId, semester).toList()
    return ResponseEntity.ok(schedule)
  }

  override fun getAppointments(
      scheduleId: String,
  ): ResponseEntity<List<Appointment>> {
    val schedule =
        scheduleManagementService.getSchedule(scheduleId)
            ?: return ResponseEntity.notFound().build()

    val appointments = mutableListOf<Appointment>()

    scheduleMetrics.recordAppointmentGeneration(scheduleId) {
      for (module in schedule.modules) {
        for (appointment in module.appointments) {
          val type =
              try {
                Appointment.AppointmentType.forValue(appointment.type)
              } catch (_: NoSuchElementException) {
                logger.error("Failed to map appointment type: ${appointment.type}")
                continue
              }

          val weekdays = appointment.appointment.weekdays ?: continue

          appointments.add(
              Appointment(
                  appointmentType = type,
                  moduleCode = module.module.code,
                  moduleTitle = module.module.title,
                  seriesBeginDate = appointment.appointment.seriesBeginDate,
                  seriesEndDate = appointment.appointment.seriesEndDate,
                  beginTime = appointment.appointment.beginTime,
                  endTime = appointment.appointment.endTime,
                  weekdays =
                      weekdays.map { weekday ->
                        Appointment.Weekdays.forValue(weekday.toString())
                      }))
        }
      }
    }

    scheduleMetrics.recordAppointmentCount(scheduleId, appointments.size)

    return ResponseEntity.ok(appointments)
  }
}
