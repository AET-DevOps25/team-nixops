package com.nixops.scraper.controller.openapi

import com.nixops.openapi.api.StudyProgramsApi
import com.nixops.openapi.model.StudyProgram
import com.nixops.scraper.services.StudyProgramService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class StudyProgramsApiController(private val studyProgramService: StudyProgramService) :
    StudyProgramsApi {
  override fun getStudyPrograms(): ResponseEntity<List<StudyProgram>> {
    val studyPrograms =
        studyProgramService.getStudyPrograms().map { studyProgram ->
          StudyProgram(
              studyId = studyProgram.studyId.toString(),
              programName = studyProgram.programName,
              degreeProgramName = studyProgram.degreeProgramName,
              degreeTypeName = studyProgram.degreeTypeName,
          )
        }
    return ResponseEntity.ok(studyPrograms)
  }
}
