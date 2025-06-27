package com.nixops.scraper.controller.openapi

import com.nixops.openapi.api.SemestersApi
import com.nixops.openapi.model.Semester
import com.nixops.scraper.services.SemesterService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class SemestersApiController(private val semesterService: SemesterService) : SemestersApi {
  override fun getSemesters(): ResponseEntity<List<Semester>> {
    val semesters =
        semesterService.getSemesters().map { semester ->
          Semester(
              semesterKey = semester.id.value,
              semesterTitle = semester.semesterTitle,
              semesterTag = semester.semesterTag)
        }
    return ResponseEntity.ok(semesters)
  }

  override fun getCurrentSemester(): ResponseEntity<Semester> {
    val semester =
        semesterService.getSemester("lecture") ?: return ResponseEntity.notFound().build()

    val apiSemester =
        Semester(
            semesterKey = semester.id.value,
            semesterTitle = semester.semesterTitle,
            semesterTag = semester.semesterTag)

    return ResponseEntity.ok(apiSemester)
  }
}
