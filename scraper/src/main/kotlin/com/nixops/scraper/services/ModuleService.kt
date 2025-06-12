package com.nixops.scraper.services

import com.nixops.scraper.model.Module
import com.nixops.scraper.repository.ModuleRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ModuleServiceService(private val moduleRepository: ModuleRepository) {

    fun getModuleById(id: Long): Module? {
        val module = moduleRepository.findByIdOrNull(id)
        if (module != null) {
            println("got module")
        } else {
            println("fetch module")
        }
        return module
    }
}
