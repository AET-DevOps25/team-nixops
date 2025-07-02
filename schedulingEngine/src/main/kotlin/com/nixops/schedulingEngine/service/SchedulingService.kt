package com.nixops.schedulingEngine.service

import com.nixops.schedulingEngine.api.ScheduleDelegate
import com.nixops.schedulingEngine.model.*
import java.time.OffsetDateTime
import java.time.ZoneOffset
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
    // Dummy implementation - replace with real logic
    val dummyEvent =
        Event(
            title = "Sample Event",
            type = "Lecture",
            timeSlot =
                TimeSlot(
                    startTime = OffsetDateTime.of(2025, 7, 7, 10, 0, 0, 0, ZoneOffset.ofHours(2)),
                    endTime = OffsetDateTime.of(2025, 7, 7, 12, 0, 0, 0, ZoneOffset.ofHours(2))))
    val schedule = Schedule(events = listOf(dummyEvent), score = 100, collisions = emptyList())
    return Response(scheduled = schedule, complement = emptyList())
  }
}
