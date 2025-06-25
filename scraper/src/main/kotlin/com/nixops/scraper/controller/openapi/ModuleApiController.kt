package com.nixops.scraper.controller.openapi

import com.nixops.openapi.api.ModulesApi
import com.nixops.openapi.model.Module
import com.nixops.scraper.services.ModuleService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class ModuleApiController(private val moduleService: ModuleService) : ModulesApi {
  override fun modulesGet(studyId: Long, semesterKey: String): ResponseEntity<List<Module>> {
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

  override fun modulesModuleCodeGet(moduleCode: String): ResponseEntity<Module> {
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
}
