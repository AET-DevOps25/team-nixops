package com.nixops.scraper.controller.openapi

import com.nixops.openapi.api.StudyProgramsApi
import com.nixops.openapi.model.*
import com.nixops.scraper.mapper.ModuleMapper
import com.nixops.scraper.services.CourseService
import com.nixops.scraper.services.ModuleService
import com.nixops.scraper.services.SemesterService
import com.nixops.scraper.services.StudyProgramService
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class StudyProgramsApiController(
    private val studyProgramService: StudyProgramService,
    private val semesterService: SemesterService,
    private val moduleService: ModuleService,
    private val courseService: CourseService,
    //
    private val moduleMapper: ModuleMapper
) : StudyProgramsApi {
  override fun getStudyPrograms(): ResponseEntity<List<StudyProgram>> {
    val studyPrograms =
        studyProgramService.getStudyPrograms().map { studyProgram ->
          StudyProgram(
              studyId = studyProgram.studyId,
              programName = studyProgram.programName,
              degreeProgramName = studyProgram.degreeProgramName,
              degreeTypeName = studyProgram.degreeTypeName,
          )
        }
    return ResponseEntity.ok(studyPrograms)
  }

  override fun getStudyProgram(studyId: Long, semesterKey: String): ResponseEntity<StudyProgram> {
    val studyProgram =
        studyProgramService.getStudyProgram(studyId) ?: return ResponseEntity.notFound().build()

    val semester =
        semesterService.getSemester(semesterKey) ?: return ResponseEntity.notFound().build()

    val modules =
        moduleService.getModules(studyId, semester) ?: return ResponseEntity.notFound().build()

    val apiModules = transaction {
      modules.map { module ->
        val courses = courseService.getCourses(module, semester)

        moduleMapper.moduleToApiModule(module, courses)
      }
    }

    val apiStudyProgram =
        StudyProgram(
            studyId = studyProgram.studyId,
            programName = studyProgram.programName,
            degreeProgramName = studyProgram.degreeProgramName,
            degreeTypeName = studyProgram.degreeTypeName,
            semesters = mapOf(semester.id.value to apiModules))

    return ResponseEntity.ok(apiStudyProgram)
  }
}
