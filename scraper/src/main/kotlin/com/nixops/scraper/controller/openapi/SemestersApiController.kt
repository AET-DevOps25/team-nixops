package com.nixops.scraper.controller.openapi

import com.nixops.openapi.api.SemestersApi
import com.nixops.openapi.model.Semester
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class SemestersApiController : SemestersApi {
  override fun semestersGet(): ResponseEntity<List<Semester>> {
    val semesters = transaction {
      com.nixops.scraper.model.Semester.all().map { semester ->
        Semester(
            semesterKey = semester.id.value,
            semesterTitle = semester.semesterTitle,
            semesterTag = semester.semesterTag)
      }
    }
    return ResponseEntity.ok(semesters)
  }
}
