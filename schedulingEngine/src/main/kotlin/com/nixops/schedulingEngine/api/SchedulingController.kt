package com.nixops.schedulingEngine.api

import com.nixops.schedulingEngine.engine.SchedulingEngine
import com.nixops.schedulingEngine.model.Query
import com.nixops.schedulingEngine.model.Response
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class SchedulingController(private val schedulingEngine: SchedulingEngine) {
  @PostMapping("/schedule")
  fun createSchedule(@RequestBody query: Query): ResponseEntity<Response> {
    val response = schedulingEngine.createSchedule(query)
    return ResponseEntity.ok(response)
  }
}
