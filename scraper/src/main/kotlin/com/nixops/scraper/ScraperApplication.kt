package com.nixops.scraper

import com.nixops.scraper.model.*
import com.nixops.scraper.services.*
import com.nixops.scraper.tum_api.campus.api.CampusCourseApiClient
import com.nixops.scraper.tum_api.nat.api.NatCourseApiClient
import com.nixops.scraper.tum_api.nat.api.NatSemesterApiClient
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@SpringBootApplication
@RestController
class ScraperApplication(
    private val campusCourseClient: CampusCourseApiClient,
    private val courseClient: NatCourseApiClient,
    //
    private val scraperService: ScraperService,
    private val semesterApiClient: NatSemesterApiClient,
) {

  /*
  @Transactional
  @GetMapping("/hello")
  fun hello(
      @RequestParam(value = "query", defaultValue = "M.Sc. Informatik") query: String,
      @RequestParam(value = "spo", defaultValue = "20231") spo: String
  ): String {
    try {
      // 1. Fetch current semester (lecture semester)
      val semester = semesterService.getCurrentLectureSemester()
      println("Semester:")
      println("semester title: ${semester.semesterTitle}")
      println("semester tag: ${semester.semesterTag}")
      println("semester key: ${semester.id}")
      println("semester id: ${semester.semesterIdTumOnline}")
      println()

      // 2. Fetch study program
      println("Study Program:")
      val program = programService.searchProgramWithSpo(query, spo)
      if (program == null) {
        println("No program found")
        return "Abort"
      }

      println("study id: ${program.id}")
      println("spo version: ${program.spoVersion}")

      val longName =
          "${program.programName} [${program.spoVersion}], ${program.degree.degreeTypeName}"
      println("long name: $longName")

      // 3. Fetch curriculum
      val curriculum = curriculumService.getCurriculumByProgramName(semester.id.value, longName)

      val selectedCurriculumId = curriculum?.id
      if (selectedCurriculumId == null) {
        println("No suitable program and curriculum found, aborting.")
        return "Abort"
      }

      // 4. Preload Modules
      println("Modules:")
      val modules = moduleService.getModules()
      val extraModuleMapping = mutableMapOf<Int, MutableList<Int>>()
      /*for (module in modules) {
          for (semesterCourses in module.semesterCourses) {
              if (semesterCourses.semester != semester.semesterKey) {
                  continue
              }
              for (course in semesterCourses.courses) {
                  val courseId = course.id
                  module.moduleId?.let {
                      extraModuleMapping.getOrPut(courseId) { mutableListOf() }.add(it)
                  }
              }
              break
          }
      }*/
      println()

      // 5. Fetch Courses
      println("Fetch Courses:")
      val courses =
          semester.semesterIdTumOnline?.let {
            campusCourseClient.getCourses(selectedCurriculumId.value, it)
          }

      // 6. Analyze courses for modules
      println("Courses:")

      var num = 0
      if (courses != null) {
        for (course in courses) {
          val detailedCourse = courseClient.getCourseById(course.id)

          println("course: ${course.id} ${course.courseTitle?.value}")

          val moduleIds =
              if (detailedCourse.modules.isNotEmpty()) {
                detailedCourse.modules.map { it.moduleId }
              } else if (extraModuleMapping.containsKey(course.id)) {
                extraModuleMapping[course.id]
              } else {
                println("No module found :( {${detailedCourse.org?.orgId}}")
                num += 1
                listOf()
              }

          if (moduleIds != null) {
            for (moduleId in moduleIds) {
              val module = moduleId?.let { moduleService.getModuleById(it) }
              if (module != null) {
                println("module: ${module.moduleTitle}")
              } else {
                println("could not find module with id: $moduleId")
              }
            }
          }

          println()
        }
      }
      println("No module found for $num courses")
    } catch (e: Exception) {
      println("An error occurred: ${e.message}")
    }

    return "Hallo Welt"
  }
   */

  @GetMapping("/check")
  fun check(): String {
    scraperService.check()
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

  fun getModuleIdsForStudyProgram(studyProgram: StudyProgram, semester: Semester): Set<Int> {
    val longName =
        "${studyProgram.programName} [${studyProgram.spoVersion}], ${studyProgram.degreeTypeName}"
    println("long name: $longName")

    val curriculum =
        transaction { Curriculum.find(Curriculums.name eq longName).firstOrNull() }
            ?: return setOf()

    val courses = campusCourseClient.getCourses(curriculum.id.value, semester.semesterIdTumOnline)

    val moduleIds = mutableSetOf<Int>()
    for (course in courses) {
      println("course: ${course.id} ${course.courseTitle.value}")

      transaction {
        ModuleCourses.select(ModuleCourses.module)
            .where {
              (ModuleCourses.semester eq semester.id.value) and (ModuleCourses.course eq course.id)
            }
            .withDistinct()
            .map { it[ModuleCourses.module] }
            .toCollection(moduleIds)
      }
    }

    return moduleIds
  }

  fun getModuleIdsForStudyProgramMerged(studyId: Long, semester: Semester): Set<Int> {
    val moduleIds = mutableSetOf<Int>()
    transaction {
      StudyProgram.find { (StudyPrograms.studyId eq studyId) }
          .forEach { studyProgram ->
            val newModuleIds = getModuleIdsForStudyProgram(studyProgram, semester)
            moduleIds.addAll(newModuleIds)
          }
    }
    return moduleIds
  }

  @GetMapping("/courses", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun courses(
      @RequestParam(value = "study_id", defaultValue = "163016030") studyId: Long,
      @RequestParam(value = "semester", defaultValue = "2025s") semesterKey: String,
  ): ResponseEntity<String> {
    val natSemester = semesterApiClient.getSemester(semesterKey)
    val semester =
        transaction { Semester.findById(natSemester.semesterKey) }
            ?: return ResponseEntity.notFound().build()

    val moduleIds = getModuleIdsForStudyProgramMerged(studyId, semester)

    val allModules = mutableListOf<Module>()
    transaction {
      moduleIds.forEach { moduleId -> Module.findById(moduleId)?.let { allModules.add(it) } }
    }

    for (module in allModules) {
      println("module: ${module.moduleCode} ${module.moduleTitle} ${module.id}")
    }

    println("found ${allModules.count()} modules")

    return ResponseEntity.ok("done")
  }
}

fun main(args: Array<String>) {
  runApplication<ScraperApplication>(*args)
}
