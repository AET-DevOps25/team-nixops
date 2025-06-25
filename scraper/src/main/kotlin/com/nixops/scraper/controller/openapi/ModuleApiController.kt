package com.nixops.scraper.controller.openapi

import com.nixops.openapi.api.ModulesApi
import com.nixops.openapi.model.Course
import com.nixops.openapi.model.Module
import com.nixops.scraper.services.CourseService
import com.nixops.scraper.services.ModuleService
import com.nixops.scraper.services.SemesterService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class ModuleApiController(
    private val moduleService: ModuleService,
    private val courseService: CourseService,
    private val semesterService: SemesterService,
) : ModulesApi {
  override fun getModules(studyId: Long, semesterKey: String): ResponseEntity<List<Module>> {
    val modules =
        moduleService.getModules(studyId, semesterKey).map { module ->
          Module(
              moduleId = module.id.value.toString(),
              moduleCode = module.moduleCode,
              moduleTitle = module.moduleTitle,
              moduleTitleEn = module.moduleTitleEn,
              moduleContent = module.moduleContent,
              moduleContentEn = module.moduleContentEn,
              moduleOutcome = module.moduleOutcome,
              moduleOutcomeEn = module.moduleOutcomeEn,
              moduleMethods = module.moduleMethods,
              moduleMethodsEn = module.moduleMethodsEn,
              moduleExam = module.moduleExam,
              moduleExamEn = module.moduleExamEn)
        }

    return ResponseEntity.ok(modules)
  }

  override fun getModuleByCode(moduleCode: String): ResponseEntity<Module> {
    val module = moduleService.getModule(moduleCode)

    return if (module != null) {
      val apiModule =
          Module(
              moduleId = module.id.value.toString(),
              moduleCode = module.moduleCode,
              moduleTitle = module.moduleTitle,
              moduleTitleEn = module.moduleTitleEn,
              moduleContent = module.moduleContent,
              moduleContentEn = module.moduleContentEn,
              moduleOutcome = module.moduleOutcome,
              moduleOutcomeEn = module.moduleOutcomeEn,
              moduleMethods = module.moduleMethods,
              moduleMethodsEn = module.moduleMethodsEn,
              moduleExam = module.moduleExam,
              moduleExamEn = module.moduleExamEn)
      ResponseEntity.ok(apiModule)
    } else {
      ResponseEntity.notFound().build()
    }
  }

  override fun getCoursesByModuleCode(
      moduleCode: String,
      semesterKey: String
  ): ResponseEntity<List<Course>> {
    val module = moduleService.getModule(moduleCode) ?: return ResponseEntity.notFound().build()
    val semester =
        semesterService.getSemester(semesterKey) ?: return ResponseEntity.notFound().build()

    val courses =
        courseService.getCourses(module, semester).map { course ->
          Course(
              courseName = course.courseName,
              courseNameEn = course.courseNameEn,
              courseNameList = course.courseNameList,
              courseNameListEn = course.courseNameListEn,
              description = course.description,
              descriptionEn = course.descriptionEn,
              teachingMethod = course.teachingMethod,
              teachingMethodEn = course.teachingMethodEn,
              note = course.note,
              noteEn = course.noteEn,
          )
        }

    return ResponseEntity.ok(courses)
  }
}
