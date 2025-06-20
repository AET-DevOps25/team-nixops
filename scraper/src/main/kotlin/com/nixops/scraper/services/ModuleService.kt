package com.nixops.scraper.services

import com.nixops.scraper.mapper.ModuleMapper
import com.nixops.scraper.model.Module
import com.nixops.scraper.repository.ModuleRepository
import com.nixops.scraper.tum_api.nat.api.NatModuleApiClient
import com.nixops.scraper.tum_api.nat.api.mapNotNullIndexed
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ModuleService(
    private val moduleRepository: ModuleRepository,
    private val moduleApiClient: NatModuleApiClient,
    private val moduleMapper: ModuleMapper,
) {
  @Transactional
  fun getModuleById(id: Int): Module? {
    return moduleRepository.findByModuleId(id)
  }

  @Transactional
  fun getModuleByCode(code: String): Module? {
    val module = moduleRepository.findByModuleCode(code)
    if (module != null) {
      println("module from db")
      return module
    } else {
      println("fetch module")
      val natModule = moduleApiClient.fetchNatModuleDetail(code)
      val newModule = moduleMapper.natModuleToModule(natModule)
      return moduleRepository.save(newModule)
    }
  }

  @Transactional
  fun getModulesByOrg(org: Int): List<Module> {
    val modules = moduleApiClient.fetchAllNatModules(org)
    return modules.mapNotNullIndexed { index, natModule ->
      natModule.moduleCode?.let {
        println("Fetching detail for module ${index + 1} of ${modules.size}: $it")
        getModuleByCode(it)
      }
    }
  }

  @Transactional fun getModules(): List<Module> = getModulesByOrg(1)
}
