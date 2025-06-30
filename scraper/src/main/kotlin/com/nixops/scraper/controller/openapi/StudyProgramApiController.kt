package com.nixops.scraper.controller.openapi

import com.nixops.openapi.api.StudyProgramsApi
import com.nixops.openapi.model.*
import com.nixops.scraper.mapper.StudyProgramMapper
import com.nixops.scraper.services.CourseService
import com.nixops.scraper.services.ModuleService
import com.nixops.scraper.services.SemesterService
import com.nixops.scraper.services.StudyProgramService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class StudyProgramsApiController(
    private val studyProgramService: StudyProgramService,
    private val semesterService: SemesterService,
    private val moduleService: ModuleService,
    private val courseService: CourseService,
    //
    private val studyProgramMapper: StudyProgramMapper,
) : StudyProgramsApi {
  override fun getStudyPrograms(): ResponseEntity<List<StudyProgram>> {
    val studyPrograms =
        studyProgramService.getStudyPrograms().map { studyProgram ->
          studyProgramMapper.studyProgramToApiStudyProgram(studyProgram)
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

    val semesterModules =
        mapOf(
            semesterKey to
                modules.map { module ->
                  val courses = courseService.getCourses(module, semester)
                  Pair(module, courses)
                })

    val apiStudyProgram =
        studyProgramMapper.studyProgramToApiStudyProgram(studyProgram, semesterModules)

    return ResponseEntity.ok(apiStudyProgram)
  }
}
