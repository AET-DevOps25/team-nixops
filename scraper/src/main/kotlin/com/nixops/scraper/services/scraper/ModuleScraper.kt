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
}
