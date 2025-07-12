package com.nixops.schedulemanager.controller

import com.nixops.openapi.schedulemanager.api.ScheduleApi
import com.nixops.schedulemanager.services.ScheduleManagementService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class ScheduleApiController(private val scheduleManagementService: ScheduleManagementService) :
    ScheduleApi {
  override fun addModule(scheduleId: String, body: String): ResponseEntity<Unit> {
    scheduleManagementService.addModule(scheduleId, body)
    return ResponseEntity.ok().build()
  }

  override fun getModules(scheduleId: String): ResponseEntity<List<String>> {
    val schedule = scheduleManagementService.getSchedule(scheduleId)

    return if (schedule == null) {
      ResponseEntity.notFound().build()
    } else {
      ResponseEntity.ok(schedule)
    }
  }
}
