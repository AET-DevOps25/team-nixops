package com.nixops.schedulingEngine.engine

import com.nixops.schedulingEngine.model.*
import java.time.LocalDate
import java.time.LocalTime
import org.springframework.stereotype.Service

@Service
class SchedulingEngineImpl : SchedulingEngine {
  override fun createSchedule(query: Query): Response {
    // Dummy implementation - replace with real logic
    val dummyEvent =
        Event(
            title = "Sample Event",
            type = "Lecture",
            timeSlot =
                TimeSlot(
                    date = LocalDate.now(),
                    startTime = LocalTime.of(10, 0),
                    endTime = LocalTime.of(12, 0)))
    val schedule = Schedule(events = listOf(dummyEvent), score = 100, collisions = emptyList())
    return Response(scheduled = schedule, complement = emptyList())
  }
}
