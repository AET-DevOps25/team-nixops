package com.nixops.scraper.services.scraper

import com.nixops.scraper.model.*
import com.nixops.scraper.tum_api.nat.api.NatModuleApiClient
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class ModuleScraper(
    private val moduleApiClient: NatModuleApiClient,
) {
  fun scrapeModuleByCode(code: String): Module? {
    return transaction {
      val natModule = moduleApiClient.fetchNatModuleDetail(code)
      println("Saving module with id: $code")

      natModule.courses.let {
        for ((semester, courses) in natModule.courses.entries) {
          for (course in courses) {
            ModuleCourses.insertIgnore {
              it[ModuleCourses.semester] = semester
              it[ModuleCourses.module] = natModule.id
              it[ModuleCourses.course] = course.courseId
            }
          }
        }
      }

      val existing = Module.findById(natModule.id)
      if (existing != null) {
        existing.moduleCode = natModule.code
        existing.moduleTitle = natModule.title
        existing.moduleTitleEn = natModule.titleEn
        existing.moduleContent = natModule.content
        existing.moduleContentEn = natModule.contentEn
        existing.moduleOutcome = natModule.outcome
        existing.moduleOutcomeEn = natModule.outcomeEn
        existing.moduleMethods = natModule.methods
        existing.moduleMethodsEn = natModule.methodsEn
        existing.moduleExam = natModule.exam
        existing.moduleExamEn = natModule.examEn
        existing
      } else {
        Module.new(natModule.id) {
          moduleCode = natModule.code
          moduleTitle = natModule.title
          moduleTitleEn = natModule.titleEn
          moduleContent = natModule.content
          moduleContentEn = natModule.contentEn
          moduleOutcome = natModule.outcome
          moduleOutcomeEn = natModule.outcomeEn
          moduleMethods = natModule.methods
          moduleMethodsEn = natModule.methodsEn
          moduleExam = natModule.exam
          moduleExamEn = natModule.examEn
        }
      }
    }
  }

  fun scrapeModulesByOrg(org: Int): List<Module> {
    val modules = moduleApiClient.fetchAllNatModules(org)
    return modules.mapIndexedNotNull { index, natModule ->
      natModule.code.let { code ->
        println("Fetching detail for module ${index + 1} of ${modules.size}: $code")
        scrapeModuleByCode(code)
      }
    }
  }

  fun scrapeModules() {
    scrapeModulesByOrg(1)
  }
}
