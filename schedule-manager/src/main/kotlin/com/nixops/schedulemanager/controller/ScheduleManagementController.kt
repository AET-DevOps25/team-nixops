package com.nixops.schedulemanager.controller

import com.nixops.schedulemanager.services.ScheduleManagementService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/schedule")
class ScheduleManagementController(
    private val scheduleManagementService: ScheduleManagementService
) {

  @PostMapping("/{id}/add")
  fun addModule(@PathVariable id: String, @RequestParam module: String) {
    scheduleManagementService.addModule(id, module)
  }

  @GetMapping("/{user_id}")
  fun getSchedule(@PathVariable id: String): List<String>? {
    return scheduleManagementService.getSchedule(id)
  }
}
