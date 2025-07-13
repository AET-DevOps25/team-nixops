package com.nixops.schedulemanager.services

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import com.github.benmanes.caffeine.cache.RemovalListener
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
  private val scheduleCache: Cache<String, Schedule> =
      Caffeine.newBuilder()
          .expireAfterAccess(1, TimeUnit.SECONDS)
          .removalListener(
              RemovalListener<String, Schedule> { key, _, cause ->
                logger.info("Schedule removed: $key, $cause")
                if (cause == RemovalCause.EXPIRED) {
                  logger.info("Schedule expired: $key")
                }
              })
          .build()

  fun getOrCreateSchedule(scheduleId: String, semester: String): Schedule {
    val schedule = scheduleCache.getIfPresent(scheduleId)
    return if (schedule == null) {
      logger.info("Created schedule $scheduleId with semester $semester")
      val newSchedule =
          Schedule(scheduleId = scheduleId, semester = semester, modules = mutableSetOf())
      scheduleMetrics.recordScheduleCreated(scheduleId, semester)
      scheduleCache.put(scheduleId, newSchedule)
      newSchedule
    } else {
      if (schedule.semester != semester) {
        logger.info(
            "Semester changed for schedule $scheduleId from ${schedule.semester} to $semester")
        schedule.semester = semester
        schedule.modules.clear()
        scheduleMetrics.recordScheduleCreated(scheduleId, semester)
      }
      schedule
    }
  }

  fun addModule(scheduleId: String, moduleCode: String, semester: String) {
    val schedule = getOrCreateSchedule(scheduleId, semester)
    if (schedule.modules.any { module -> module.module.code == moduleCode }) {
      return
    }
    val module = fetchModule(moduleCode, schedule.semester)
    if (module != null && schedule.modules.add(module)) {
      scheduleMetrics.recordModuleAdded(scheduleId, moduleCode)
    }
  }

  fun removeModule(scheduleId: String, moduleCode: String, semester: String) {
    val schedule = getOrCreateSchedule(scheduleId, semester)
    if (schedule.modules.removeIf { it.module.code == moduleCode }) {
      scheduleMetrics.recordModuleRemoved(scheduleId, moduleCode)
    }
  }

  fun getModuleCodes(scheduleId: String, semester: String): List<String> {
    val schedule = getOrCreateSchedule(scheduleId, semester)
    return schedule.modules.mapNotNull { it.module.code }
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
}
