package com.nixops.schedulemanager.services

import com.github.benmanes.caffeine.cache.Caffeine
import com.nixops.openapi.scraper.api.DefaultApi
import com.nixops.openapi.scraper.infrastructure.ClientException
import com.nixops.schedulemanager.metrics.ScheduleMetrics
import com.nixops.schedulemanager.model.Appointment
import com.nixops.schedulemanager.model.Module
import com.nixops.schedulemanager.model.Schedule
import java.util.concurrent.TimeUnit
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ScheduleManagementService(
    private val scraperApiClient: DefaultApi,
    //
    private val scheduleMetrics: ScheduleMetrics,
) {
  private val scheduleCache =
      Caffeine.newBuilder().expireAfterAccess(12, TimeUnit.HOURS).build<String, Schedule>()

  fun createSchedule(scheduleId: String, studyId: Long, semester: String): Schedule {
    val newSchedule =
        Schedule(
            scheduleId = scheduleId,
            studyId = studyId,
            semester = semester,
            modules = mutableSetOf())
    scheduleCache.put(scheduleId, newSchedule)
    scheduleMetrics.recordScheduleCreated(scheduleId, studyId, semester)
    return newSchedule
  }

  fun addModule(scheduleId: String, moduleCode: String) {
    scheduleCache.asMap().computeIfPresent(scheduleId) { _, schedule ->
      val module = fetchModule(moduleCode, schedule.semester)
      if (module != null) {
        schedule.modules.add(module)
        scheduleMetrics.recordModuleAdded(scheduleId, moduleCode)
      }
      schedule
    }
  }

  fun removeModule(scheduleId: String, moduleCode: String) {
    scheduleCache.asMap().computeIfPresent(scheduleId) { _, schedule ->
      if (schedule.modules.removeIf { module -> module.module.code == moduleCode }) {
        scheduleMetrics.recordModuleRemoved(scheduleId, moduleCode)
      }
      schedule
    }
  }

  fun getModuleCodes(scheduleId: String): List<String> {
    return scheduleCache.getIfPresent(scheduleId)?.modules?.mapNotNull { module ->
      module.module.code
    } ?: emptyList()
  }

  fun fetchModule(moduleCode: String, semesterKey: String): Module? {
    try {
      logger.info("Fetching module: $moduleCode, $semesterKey")
      val scraperModule = scraperApiClient.getModuleByCode(moduleCode)

      logger.info("Fetching courses for module: $moduleCode, $semesterKey")
      val courses = scraperApiClient.getCoursesByModuleCode(moduleCode, semesterKey)

      val appointments = mutableListOf<Appointment>()

      for (course in courses) {
        val type =
            when (course.courseType) {
              "Vorlesung",
              "Vorlesung mit integrierten Übungen" -> {
                "lecture"
              }
              "Übung",
              "Tutorium" -> {
                "tutorial"
              }
              else -> null
            } ?: continue

        val courseAppointments = course.appointments ?: continue

        for (appointment in courseAppointments) {
          appointments.add(Appointment(type, appointment))
        }
      }

      return Module(scraperModule, appointments)
    } catch (e: ClientException) {
      logger.error(e) { "Failed to fetch module: $moduleCode, $semesterKey" }
      return null
    }
  }

  fun getSchedule(scheduleId: String): Schedule? {
    return scheduleCache.getIfPresent(scheduleId)
  }
}
