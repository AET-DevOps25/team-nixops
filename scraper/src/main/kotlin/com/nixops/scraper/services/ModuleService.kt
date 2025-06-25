package com.nixops.scraper.services

import com.nixops.scraper.mapper.ModuleMapper
import com.nixops.scraper.model.Module
import com.nixops.scraper.model.Modules
import com.nixops.scraper.tum_api.nat.api.NatModuleApiClient
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class ModuleService(
    private val moduleApiClient: NatModuleApiClient,
    private val moduleMapper: ModuleMapper,
) {
  fun getModuleById(id: Int): Module? = transaction { Module.findById(id) }

  fun getModuleByCode(code: String): Module? = transaction {
    val module = Module.find { Modules.moduleCode eq code }.firstOrNull()
    if (module != null) {
      println("module from db")
      module
    } else {
      println("fetch module")
      val natModule = moduleApiClient.fetchNatModuleDetail(code)
      val newModule = moduleMapper.natModuleToModule(natModule)
      newModule
    }
  }

  fun getModulesByOrg(org: Int): List<Module> {
    val modules = moduleApiClient.fetchAllNatModules(org)
    return modules.mapIndexedNotNull { index, natModule ->
      natModule.moduleCode?.let { code ->
        println("Fetching detail for module ${index + 1} of ${modules.size}: $code")
        getModuleByCode(code)
      }
    }
  }

  fun getModules(): List<Module> = getModulesByOrg(1)
}
