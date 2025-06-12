package com.nixops.scraper.controller

import com.nixops.scraper.model.Module
import com.nixops.scraper.services.ModuleService
import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/modules")
class ModuleController(
    private val moduleService: ModuleService
) {
    @GetMapping("/{code}")
    @Transactional
    fun getModuleByCode(@PathVariable code: String): ResponseEntity<List<String>> {
        val module = moduleService.getModuleByCode(code)
        val courseNames = module?.semesterCourses
            ?.flatMap { it.courses.map { course -> course.courseName } }

        return if (courseNames != null) {
            ResponseEntity.ok(courseNames)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/by-org/{org}")
    @Transactional
    fun getModulesByOrg(@PathVariable org: Int): ResponseEntity<List<String>> {
        val modules = moduleService.getModulesByOrg(org)
        val moduleNames = modules.mapNotNull { it.moduleTitle};

        return ResponseEntity.ok(moduleNames)
    }
}
