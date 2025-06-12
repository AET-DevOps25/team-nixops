package com.nixops.scraper.controller

import com.nixops.scraper.model.Program
import com.nixops.scraper.repository.ProgramRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/programs")
class ProgramController(private val programRepository: ProgramRepository) {

    @GetMapping
    fun getAll(): List<Program> = programRepository.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Program? = programRepository.findById(id).orElse(null)
}
