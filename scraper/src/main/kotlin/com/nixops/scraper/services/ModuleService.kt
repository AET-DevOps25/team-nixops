package com.nixops.scraper.services

import com.nixops.scraper.model.*
import com.nixops.scraper.services.scraper.ModuleScraper
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class ModuleService(
    private val courseService: CourseService,
    private val semesterService: SemesterService,
    private val moduleScraper: ModuleScraper,
) {
  fun getModule(code: String): Module? {
    return transaction { Module.find(Modules.moduleCode eq code).firstOrNull() }
        ?: moduleScraper.scrapeModuleByCode(code)
  }

  fun getModuleIds(studyProgram: StudyProgram, semester: Semester): Set<Int> {
    val courses = courseService.getCourses(studyProgram, semester)

    val moduleIds = mutableSetOf<Int>()
    for (course in courses) {
      transaction {
        ModuleCourses.select(ModuleCourses.module)
            .where {
              (ModuleCourses.semester eq semester.id.value) and
                  (ModuleCourses.course eq course.id.value)
            }
            .withDistinct()
            .map { it[ModuleCourses.module] }
            .toCollection(moduleIds)
      }
    }

    return moduleIds
  }

  fun getModuleIds(studyId: Long, semester: Semester): Set<Int>? {
    val moduleIds = mutableSetOf<Int>()

    val studyPrograms = transaction {
      StudyProgram.find { (StudyPrograms.studyId eq studyId) }.toList()
    }

    if (studyPrograms.isEmpty()) {
      return null
    }

    transaction {
      studyPrograms.forEach { studyProgram ->
        val newModuleIds = getModuleIds(studyProgram, semester)
        moduleIds.addAll(newModuleIds)
      }
    }

    return moduleIds
  }

  fun getModules(studyId: Long, semester: Semester): List<Module>? {
    val allModules = mutableListOf<Module>()
    val moduleIds = transaction { getModuleIds(studyId, semester) } ?: return null
    transaction {
      moduleIds.forEach { moduleId -> Module.findById(moduleId)?.let { allModules.add(it) } }
    }
    return allModules
  }

  fun getModules(studyId: Long, semesterKey: String): List<Module>? {
    val semester = semesterService.getSemester(semesterKey) ?: return null
    return getModules(studyId, semester)
  }
}
