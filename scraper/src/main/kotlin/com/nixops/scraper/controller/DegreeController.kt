package com.nixops.scraper.controller

import com.nixops.scraper.model.Degree
import com.nixops.scraper.repository.DegreeRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/degrees")
class DegreeController(private val degreeRepository: DegreeRepository) {

    @GetMapping
    fun getAll(): List<Degree> = degreeRepository.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Degree? = degreeRepository.findById(id).orElse(null)
}