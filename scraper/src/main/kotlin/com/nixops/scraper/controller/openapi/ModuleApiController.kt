package com.nixops.scraper.controller.openapi

import com.nixops.openapi.api.ModulesApi
import com.nixops.openapi.model.Course
import com.nixops.openapi.model.Module
import com.nixops.scraper.mapper.CourseMapper
import com.nixops.scraper.mapper.ModuleMapper
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
    //
    private val moduleMapper: ModuleMapper,
    private val courseMapper: CourseMapper
) : ModulesApi {
  override fun getModules(studyId: Long, semesterKey: String): ResponseEntity<List<Module>> {
    val modules =
        moduleService.getModules(studyId, semesterKey) ?: return ResponseEntity.notFound().build()

    val apiModules = modules.map { module -> moduleMapper.moduleToApiModule(module) }

    return ResponseEntity.ok(apiModules)
  }

  override fun getModuleByCode(moduleCode: String): ResponseEntity<Module> {
    val module = moduleService.getModule(moduleCode)

    return if (module != null) {
      val apiModule = moduleMapper.moduleToApiModule(module)
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
          courseMapper.courseToApiCourse(course)
        }

    return ResponseEntity.ok(courses)
  }
}
