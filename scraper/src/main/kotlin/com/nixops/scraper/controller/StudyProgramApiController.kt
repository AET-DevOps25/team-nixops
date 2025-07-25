package com.nixops.scraper.controller

import com.nixops.openapi.api.StudyProgramsApi
import com.nixops.openapi.model.*
import com.nixops.scraper.mapper.StudyProgramMapper
import com.nixops.scraper.model.CurriculumCourses
import com.nixops.scraper.model.Curriculums
import com.nixops.scraper.model.Semesters
import com.nixops.scraper.model.StudyPrograms
import com.nixops.scraper.services.CourseService
import com.nixops.scraper.services.ModuleService
import com.nixops.scraper.services.SemesterService
import com.nixops.scraper.services.StudyProgramService
import org.jetbrains.exposed.sql.JoinType
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
    private val studyProgramMapper: StudyProgramMapper,
) : StudyProgramsApi {
  override fun getStudyPrograms(): ResponseEntity<List<StudyProgram>> {
    val studyPrograms =
        studyProgramService.getStudyPrograms().map { studyProgram ->
          studyProgramMapper.studyProgramToApiStudyProgram(studyProgram)
        }

    return ResponseEntity.ok(studyPrograms)
  }

  override fun getFinishedStudyPrograms(): ResponseEntity<List<StudyProgram>> {
    data class StudyProgramRow(
        val studyId: Long,
        val programName: String,
        val degreeProgramName: String,
        val degreeTypeName: String,
        val semesterId: String
    )

    val studyPrograms =
        transaction {
              CurriculumCourses.join(
                      Curriculums, JoinType.INNER, CurriculumCourses.curriculum, Curriculums.id)
                  .join(StudyPrograms, JoinType.INNER, Curriculums.name, StudyPrograms.fullName)
                  .join(
                      Semesters,
                      JoinType.INNER,
                      CurriculumCourses.semester,
                      Semesters.semesterIdTumOnline)
                  .select(
                      StudyPrograms.studyId,
                      StudyPrograms.programName,
                      StudyPrograms.degreeProgramName,
                      StudyPrograms.degreeTypeName,
                      Semesters.id)
                  .withDistinct()
                  .mapNotNull { row ->
                    StudyProgramRow(
                        studyId = row[StudyPrograms.studyId],
                        programName = row[StudyPrograms.programName],
                        degreeProgramName = row[StudyPrograms.degreeProgramName],
                        degreeTypeName = row[StudyPrograms.degreeTypeName],
                        semesterId = row[Semesters.id].value)
                  }
            }
            .groupBy { Triple(it.studyId, it.programName, it.degreeProgramName) }
            .map { (key, rows) ->
              val (studyId, programName, degreeProgramName) = key
              val degreeTypeName = rows.first().degreeTypeName
              val semesters = rows.map { it.semesterId }

              StudyProgram(
                  studyId = studyId,
                  programName = programName,
                  degreeProgramName = degreeProgramName,
                  degreeTypeName = degreeTypeName,
                  semesters = semesters.associateWith { listOf() })
            }

    return ResponseEntity.ok(studyPrograms)
  }

  override fun getStudyProgram(studyId: Long, semesterKey: String): ResponseEntity<StudyProgram> {
    val studyProgram =
        studyProgramService.getStudyProgram(studyId) ?: return ResponseEntity.notFound().build()

    val apiStudyProgram = studyProgramMapper.studyProgramToApiStudyProgram(studyProgram)

    return ResponseEntity.ok(apiStudyProgram)
  }

  override fun getFullStudyProgram(
      studyId: Long,
      semesterKey: String
  ): ResponseEntity<StudyProgram> {
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
