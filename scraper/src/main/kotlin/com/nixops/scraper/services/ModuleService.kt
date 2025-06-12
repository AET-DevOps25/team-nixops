package com.nixops.scraper.services

import com.nixops.scraper.mapper.ModuleMapper
import com.nixops.scraper.model.Module
import com.nixops.scraper.repository.ModuleRepository
import com.nixops.scraper.tum_api.nat.api.NatModuleApiClient
import org.springframework.stereotype.Service

@Service
class ModuleService(
    private val moduleRepository: ModuleRepository,
    private val moduleApiClient: NatModuleApiClient,
    private val moduleMapper: ModuleMapper,
) {
    fun getModuleByCode(code: String): Module? {
        val module = moduleRepository.findByModuleCode(code)
        if (module != null) {
            return module
        } else {
            val natModule = moduleApiClient.fetchNatModuleDetail(code)
            val newModule = moduleMapper.natModuleToModule(natModule)
            return moduleRepository.save(newModule)
        }
    }
}