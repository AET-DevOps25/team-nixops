package com.nixops.scraper.mapper

import com.nixops.scraper.model.Module
import com.nixops.scraper.tum_api.nat.model.NatModule
import org.springframework.stereotype.Component

@Component
class ModuleMapper {

  fun natModuleToModule(natModule: NatModule): Module {
    return Module.new(natModule.moduleId ?: 0) {
      moduleTitle = natModule.moduleTitle
      moduleCode = natModule.moduleCode
    }
  }
}
