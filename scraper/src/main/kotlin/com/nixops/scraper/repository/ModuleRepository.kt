package com.nixops.scraper.repository


import com.nixops.scraper.model.Module
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ModuleRepository : JpaRepository<Module, Long> {
    fun findByModuleCode(moduleCode: String): Module?
}