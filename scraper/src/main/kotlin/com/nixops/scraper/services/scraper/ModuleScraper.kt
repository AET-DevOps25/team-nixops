package com.nixops.scraper.services.scraper

import com.nixops.scraper.extensions.genericUpsert
import com.nixops.scraper.model.*
import com.nixops.scraper.tum_api.nat.api.NatModuleApiClient
import com.nixops.scraper.tum_api.nat.model.NatModule
import java.io.IOException
import mu.KotlinLogging
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ModuleScraper(
    private val moduleApiClient: NatModuleApiClient,
) {
  fun updateNatModule(natModule: NatModule): Module? {
    return transaction {
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

      Modules.genericUpsert(Module) {
        it[Modules.id] = natModule.id
        it[Modules.moduleCode] = natModule.code
        it[Modules.moduleTitle] = natModule.title
        it[Modules.moduleTitleEn] = natModule.titleEn
        it[Modules.moduleContents] = natModule.content
        it[Modules.moduleContentsEn] = natModule.contentEn
        it[Modules.moduleOutcome] = natModule.outcome
        it[Modules.moduleOutcomeEn] = natModule.outcomeEn
        it[Modules.moduleMethods] = natModule.methods
        it[Modules.moduleMethodsEn] = natModule.methodsEn
        it[Modules.moduleExam] = natModule.exam
        it[Modules.moduleExamEn] = natModule.examEn
        it[Modules.moduleCredits] = natModule.credits
      }
    }
  }

  fun scrapeModuleByCode(code: String): Module? {
    try {
      val natModule = moduleApiClient.fetchNatModuleDetail(code) ?: return null
      logger.trace("Saving module with id: $code")

      return updateNatModule(natModule)
    } catch (e: IOException) {
      return null
    }
  }

  fun scrapeModulesByOrg(org: Int): List<Module> {
    val modules = moduleApiClient.fetchAllNatModules(org)
    return modules.mapIndexedNotNull { index, natModule ->
      natModule.code.let { code ->
        logger.info("Fetching detail for module ${index + 1} of ${modules.size}: $code")
        scrapeModuleByCode(code)
      }
    }
  }

  fun scrapeModules() {
    scrapeModulesByOrg(1)
  }
}
