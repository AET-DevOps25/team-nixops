package com.nixops.scraper.services

import com.nixops.scraper.model.*
import com.nixops.scraper.tum_api.campus.api.CampusCourseApiClient
import com.nixops.scraper.tum_api.campus.api.CampusCurriculumApiClient
import com.nixops.scraper.tum_api.nat.api.NatCourseApiClient
import com.nixops.scraper.tum_api.nat.api.NatModuleApiClient
import com.nixops.scraper.tum_api.nat.api.NatProgramApiClient
import com.nixops.scraper.tum_api.nat.api.NatSemesterApiClient
import java.time.Duration
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class ScraperService(
    private val semesterApiClient: NatSemesterApiClient,
    private val studyProgramApiClient: NatProgramApiClient,
    private val curriculumApiClient: CampusCurriculumApiClient,
    private val moduleApiClient: NatModuleApiClient,
    private val campusCourseApiClient: CampusCourseApiClient,
    private val natCourseApiClient: NatCourseApiClient
) {
  fun scrapeSemester(semesterKey: String): Semester {
    return transaction {
      val natSemester = semesterApiClient.getSemester(semesterKey)
      println("Saving semester with key: ${natSemester.semesterKey}")

      val existing = Semester.findById(natSemester.semesterKey)
      if (existing != null) {
        existing.semesterTag = natSemester.semesterTag
        existing.semesterTitle = natSemester.semesterTitle
        existing.semesterIdTumOnline = natSemester.semesterIdTumOnline
        existing
      } else {
        Semester.new(natSemester.semesterKey) {
          semesterTag = natSemester.semesterTag
          semesterTitle = natSemester.semesterTitle
          semesterIdTumOnline = natSemester.semesterIdTumOnline
        }
      }
    }
  }

  fun scrapeSemesters() {
    val semesters = semesterApiClient.getSemesters()
    semesters.map { scrapeSemester(it.semesterKey) }
  }

  fun scrapeStudyPrograms() {
    val studyPrograms = studyProgramApiClient.getPrograms()
    transaction {
      studyPrograms
          .filter { it.spoVersion != "0" }
          .map {
            println("Saving study program with name: ${it.degreeProgramName}")

            val existing =
                StudyProgram.find(
                        (StudyPrograms.studyId eq it.studyId) and
                            (StudyPrograms.spoVersion eq it.spoVersion))
                    .firstOrNull()

            if (existing != null) {
              existing.orgId = it.orgId
              existing.spoVersion = it.spoVersion
              existing.programName = it.programName
              existing.degreeProgramName = it.degreeProgramName
              existing.degreeTypeName = it.degree.degreeTypeName
            } else {
              StudyProgram.new {
                studyId = it.studyId
                orgId = it.orgId
                spoVersion = it.spoVersion
                programName = it.programName
                degreeProgramName = it.degreeProgramName
                degreeTypeName = it.degree.degreeTypeName
              }
            }
          }
    }
  }

  fun scrapeCurricula(tumId: Int): List<Curriculum> {
    return transaction {
      curriculumApiClient.getCurriculaForSemester(tumId).map { apiCurriculum ->
        println("Saving curriculum with name: ${apiCurriculum.name}")

        val existing = Curriculum.findById(apiCurriculum.id)
        if (existing != null) {
          existing.name = apiCurriculum.name
          existing
        } else {
          Curriculum.new(apiCurriculum.id) { name = apiCurriculum.name }
        }
      }
    }
  }

  fun scrapeCurricula() {
    transaction {
      Semester.all().forEach { semester ->
        semester.semesterIdTumOnline?.let { scrapeCurricula(it) }
      }
    }
  }

  fun scrapeModuleByCode(code: String): Module? {
    return transaction {
      val natModule = moduleApiClient.fetchNatModuleDetail(code)
      println("Saving module with id: $code")

      natModule.courses?.let {
        for ((semester, courses) in natModule.courses.entries) {
          for (course in courses) {
            ModuleCourses.insertIgnore {
              it[ModuleCourses.semester] = semester
              it[ModuleCourses.module] = natModule.moduleId
              it[ModuleCourses.course] = course.courseId
            }
          }
        }
      }

      val existing = natModule.moduleId.let { it1 -> Module.findById(it1) }
      if (existing != null) {
        existing.moduleTitle = natModule.moduleTitle
        existing.moduleCode = natModule.moduleCode
        existing
      } else {
        Module.new(natModule.moduleId) {
          moduleTitle = natModule.moduleTitle
          moduleCode = natModule.moduleCode
        }
      }
    }
  }

  fun scrapeModulesByOrg(org: Int): List<Module> {
    val modules = moduleApiClient.fetchAllNatModules(org)
    return modules.mapIndexedNotNull { index, natModule ->
      natModule.moduleCode.let { code ->
        println("Fetching detail for module ${index + 1} of ${modules.size}: $code")
        scrapeModuleByCode(code)
      }
    }
  }

  fun scrapeModules() {
    scrapeModulesByOrg(1)
  }

  /* fun scrapeCourses() {
      transaction {
          Curriculum.all().forEach { curriculum ->
              Semester.all().forEach { semester ->
                  semester.semesterIdTumOnline?.let { tumOnlineId ->
                      if (tumOnlineId == 204) {
                          println("fetching courses for curriculum: ${curriculum.id.value} $tumOnlineId")
                          campusCourseApiClient.getCourses(curriculum.id.value, tumOnlineId)
                      }
                  }
              }
          }
      }
  } */

  fun check(name: String, scrape: () -> Unit) {
    val lastUpdated = getTimeSinceLastUpdated(name)
    if (lastUpdated == null || lastUpdated > Duration.ofHours(2)) {
      println("should update $name")
      scrape()
      setLastUpdated(name)
    }
  }

  fun check() {
    check("semesters", ::scrapeSemesters)
    check("study_programs", ::scrapeStudyPrograms)
    check("curricula", ::scrapeCurricula)
    check("modules", ::scrapeModules)
    // check("courses", ::scrapeCourses)
  }
}
