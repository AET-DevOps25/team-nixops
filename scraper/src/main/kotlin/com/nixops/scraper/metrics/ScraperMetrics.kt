package com.nixops.scraper.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component

@Component
class ScraperMetrics(private val meterRegistry: MeterRegistry) {
  fun incrementModuleCounter(moduleId: Int, moduleCode: String, moduleName: String) {
    val counterName = "scraper.modules.scraped"
    meterRegistry
        .counter(
            counterName,
            "moduleId",
            moduleId.toString(),
            "moduleCode",
            moduleCode,
            "moduleName",
            moduleName)
        .increment()
  }

  fun incrementCourseCounter(courseId: Int, courseTitle: String) {
    val counterName = "scraper.courses.scraped"
    meterRegistry
        .counter(counterName, "courseId", courseId.toString(), "courseTitle", courseTitle)
        .increment()
  }

  fun incrementAppointmentCounter(appointmentId: Int) {
    val counterName = "scraper.appointments.scraped"
    meterRegistry
        .counter(
            counterName,
            "appointmentId",
            appointmentId.toString(),
        )
        .increment()
  }

  fun incrementStudyProgramCounter(
      studyId: Long,
      programName: String,
      spoVersion: String,
      degreeType: String
  ) {
    val counterName = "scraper.study_programs.scraped"
    meterRegistry
        .counter(
            counterName,
            "studyId",
            studyId.toString(),
            "programName",
            programName,
            "spoVersion",
            spoVersion,
            "degreeType",
            degreeType)
        .increment()
  }

  fun recordScrapeDuration(entity: String, block: () -> Unit) {
    Timer.builder("scraper.duration")
        .description("Duration of scrape")
        .tag("data", entity)
        .register(meterRegistry)
        .record(block)
  }
}
