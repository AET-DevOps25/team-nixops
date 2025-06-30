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
  fun moduleToApiModule(module: com.nixops.scraper.model.Module): Module {
    return Module(
        id = module.id.value.toString(),
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
    )
  }

  override fun getModules(studyId: Long, semesterKey: String): ResponseEntity<List<Module>> {
    val modules =
        moduleService.getModules(studyId, semesterKey) ?: return ResponseEntity.notFound().build()

    val apiModules = modules.map(::moduleToApiModule)

    return ResponseEntity.ok(apiModules)
  }

  override fun getModuleByCode(moduleCode: String): ResponseEntity<Module> {
    val module = moduleService.getModule(moduleCode)

    return if (module != null) {
      val apiModule = moduleToApiModule(module)
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
              courseId = course.id.value,
              courseType = course.activityName,
              courseName = course.courseName,
              courseNameEn = course.courseNameEn,
              courseNameList = course.courseNameList,
              courseNameListEn = course.courseNameListEn,
          )
        }

    return ResponseEntity.ok(courses)
  }
}
