package com.nixops.scraper.mapper

import com.nixops.openapi.model.ModuleCourses
import com.nixops.scraper.model.Course
import com.nixops.scraper.model.Module
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class ModuleMapper(private val courseMapper: CourseMapper) {
  fun moduleToApiModule(
      module: Module,
      courses: Set<Course> = setOf()
  ): com.nixops.openapi.model.Module {
    return transaction {
      val lectures = mutableListOf<com.nixops.openapi.model.Course>()
      val tutorials = mutableListOf<com.nixops.openapi.model.Course>()
      val other = mutableListOf<com.nixops.openapi.model.Course>()

      for (course in courses) {
        val apiCourse = courseMapper.courseToApiCourse(course)

        when (course.activityName) {
          "Vorlesung",
          "Vorlesung mit integrierten Übungen" -> {
            lectures.add(apiCourse)
          }

          "Tutorium",
          "Übung" -> {
            tutorials.add(apiCourse)
          }

          else -> {
            other.add(apiCourse)
          }
        }
      }

      val moduleCourse = ModuleCourses(lectures = lectures, tutorials = tutorials, other = other)

      com.nixops.openapi.model.Module(
          id = module.id.value,
          code = module.moduleCode,
          title = module.moduleTitle,
          titleEn = module.moduleTitleEn,
          content = module.moduleContent,
          contentEn = module.moduleContentEn,
          outcome = module.moduleOutcome,
          outcomeEn = module.moduleOutcomeEn,
          methods = module.moduleMethods,
          methodsEn = module.moduleMethodsEn,
          exam = module.moduleExam,
          examEn = module.moduleExamEn,
          credits = module.moduleCredits,
          courses = moduleCourse)
    }
  }
}
