package com.nixops.scraper.controller

import com.nixops.scraper.model.Module
import com.nixops.scraper.services.ModuleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/modules")
class ModuleController(
    private val moduleService: ModuleService
) {

    @GetMapping("/{code}")
    fun getModuleByCode(@PathVariable code: String): ResponseEntity<Module> {
        val module = moduleService.getModuleByCode(code)
        return if (module != null) {
            ResponseEntity.ok(module)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
