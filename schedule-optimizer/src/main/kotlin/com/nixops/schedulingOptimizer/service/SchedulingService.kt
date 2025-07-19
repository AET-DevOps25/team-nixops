package com.nixops.scheduleOptimizer.service

import com.nixops.scheduleOptimizer.api.ScheduleDelegate
import com.nixops.scheduleOptimizer.model.*
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*

@Service
class SchedulingService : ScheduleDelegate {

  override fun schedule(query: Query): ResponseEntity<Response> {
    // TODO: implement your actual scheduling logic here

    // This is a dummy implementation with 501 NOT_IMPLEMENTED.
    // Replace with real logic and Response construction.
    return ResponseEntity.ok(createSchedule(query))
  }

  private fun createSchedule(query: Query): Response {
    val schedule = Schedule(events = emptyList(), score = 100, collisions = emptyList())
    return Response(scheduled = schedule.events, complement = emptyList())
  }
}
