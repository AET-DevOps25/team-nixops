package com.nixops.scraper.services

import com.nixops.scraper.model.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class ModuleService(
    private val courseService: CourseService,
    private val semesterService: SemesterService
) {
  fun getModuleIds(studyProgram: StudyProgram, semester: Semester): Set<Int> {
    val courses = courseService.getCourseIds(studyProgram, semester)

    val moduleIds = mutableSetOf<Int>()
    for (course in courses) {
      println("course: $course")

      transaction {
        ModuleCourses.select(ModuleCourses.module)
            .where {
              (ModuleCourses.semester eq semester.id.value) and (ModuleCourses.course eq course)
            }
            .withDistinct()
            .map { it[ModuleCourses.module] }
            .toCollection(moduleIds)
      }
    }

    return moduleIds
  }

  fun getModuleIds(studyId: Long, semester: Semester): Set<Int> {
    val moduleIds = mutableSetOf<Int>()
    transaction {
      StudyProgram.find { (StudyPrograms.studyId eq studyId) }
          .forEach { studyProgram ->
            val newModuleIds = getModuleIds(studyProgram, semester)
            moduleIds.addAll(newModuleIds)
          }
    }
    return moduleIds
  }

  fun getModules(studyId: Long, semester: Semester): List<Module> {
    val allModules = mutableListOf<Module>()
    transaction {
      getModuleIds(studyId, semester).forEach { moduleId ->
        Module.findById(moduleId)?.let { allModules.add(it) }
      }
    }
    return allModules
  }

  fun getModules(studyId: Long, semesterKey: String): List<Module> {
    val semester = semesterService.getSemester(semesterKey) ?: return listOf()

    val allModules = mutableListOf<Module>()
    transaction {
      getModuleIds(studyId, semester).forEach { moduleId ->
        Module.findById(moduleId)?.let { allModules.add(it) }
      }
    }
    return allModules
  }
}
