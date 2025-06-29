package com.nixops.scraper.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.nixops.scraper.model.StudyProgram
import com.nixops.scraper.model.StudyPrograms
import com.nixops.scraper.services.CourseService
import com.nixops.scraper.services.ModuleService
import com.nixops.scraper.services.SemesterService
import com.nixops.scraper.services.scraper.ScraperScheduler
import io.swagger.v3.oas.annotations.Parameter
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class DebugController(
    private val scraperScheduler: ScraperScheduler,
    private val moduleService: ModuleService,
    private val courseService: CourseService,
    private val semesterService: SemesterService,
    private val objectMapper: ObjectMapper
) {

  @GetMapping("/check")
  fun check(): String {
    scraperScheduler.check()
    return "done"
  }

  @GetMapping("/study_programs/search", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun studyProgramSearch(
      @RequestParam(value = "query", defaultValue = "") query: String
  ): ResponseEntity<List<Map<String, String>>> {
    val results = transaction {
      StudyProgram.find {
            (StudyPrograms.degreeProgramName like "%$query%") or
                (StudyPrograms.programName like "%$query%")
          }
          .groupBy { it.studyId }
          .mapNotNull { (_, programs) -> programs.maxByOrNull { it.spoVersion.toIntOrNull() ?: 0 } }
          .map {
            mapOf(
                "degreeProgramName" to it.degreeProgramName,
                "studyId" to it.studyId.toString(),
            )
          }
    }

    return ResponseEntity.ok(results)
  }

  @RequestMapping(
      method = [RequestMethod.GET],
      value = ["/courses/{module_code}"],
      produces = ["application/json"])
  fun getCoursesByModuleCode(
      @Parameter(description = "Code of the module", required = true)
      @PathVariable("module_code")
      moduleCode: kotlin.String,
      @RequestParam(value = "semesterKey", required = true) semesterKey: kotlin.String
  ): ResponseEntity<Any> {
    val module = moduleService.getModule(moduleCode) ?: return ResponseEntity.notFound().build()
    val semester =
        semesterService.getSemester(semesterKey) ?: return ResponseEntity.notFound().build()

    val json = transaction {
      val courses =
          courseService
              .getCourses(module, semester)
              .map { course ->
                mapOf(
                    "id" to course.id.value,
                    "name" to course.courseName,
                    "type" to course.activityName,
                    "groups" to
                        course.groups
                            .map { group ->
                              mapOf(
                                  "id" to group.id.value,
                                  "name" to group.name,
                                  "appointments" to
                                      group.appointments
                                          .map { appointment ->
                                            mapOf(
                                                "id" to appointment.id.value,
                                                "seriesStartDate" to appointment.seriesBeginDate,
                                                "seriesEndDate" to appointment.seriesEndDate,
                                                "startTime" to appointment.beginTime,
                                                "endTime" to appointment.endTime,
                                                "weekdays" to
                                                    appointment.weekdays
                                                        .map { weekday -> weekday.name }
                                                        .toList())
                                          }
                                          .toList())
                            }
                            .toList())
              }
              .toList()

      objectMapper.writeValueAsString(courses)
    }

    return ResponseEntity.ok(json)
  }
}
