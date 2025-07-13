package com.nixops.schedulemanager.metrics

import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component

@Component
class ScheduleMetrics(private val meterRegistry: MeterRegistry) {

  fun recordScheduleCreated(scheduleId: String, semester: String) {
    meterRegistry
        .counter("schedule.created", "scheduleId", scheduleId, "semester", semester)
        .increment()
  }

  fun recordModuleAdded(scheduleId: String, moduleCode: String) {
    meterRegistry
        .counter("schedule.modules.added", "scheduleId", scheduleId, "moduleCode", moduleCode)
        .increment()
  }

  fun recordModuleRemoved(scheduleId: String, moduleCode: String) {
    meterRegistry
        .counter("schedule.modules.removed", "scheduleId", scheduleId, "moduleCode", moduleCode)
        .increment()
  }

  fun recordAppointmentGeneration(scheduleId: String, block: () -> Unit) {
    Timer.builder("appointments.generation_duration")
        .tag("scheduleId", scheduleId)
        .register(meterRegistry)
        .record(block)
  }

  fun recordAppointmentCount(scheduleId: String, count: Int) {
    DistributionSummary.builder("appointments.count_per_schedule")
        .tag("scheduleId", scheduleId)
        .register(meterRegistry)
        .record(count.toDouble())
  }
}
