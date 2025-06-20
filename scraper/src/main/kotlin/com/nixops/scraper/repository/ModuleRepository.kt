package com.nixops.scraper.repository

import com.nixops.scraper.model.Module
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ModuleRepository : JpaRepository<Module, Long> {
  fun findByModuleId(moduleId: Int): Module?

  fun findByModuleCode(moduleCode: String): Module?
}
