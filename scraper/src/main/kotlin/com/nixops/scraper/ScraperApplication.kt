package com.nixops.scraper

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.*

import com.nixops.openapi.api.StudyProgramsApi
import com.nixops.scraper.mapper.DegreeMapper
import com.nixops.scraper.mapper.ProgramMapper
import com.nixops.scraper.repository.DegreeRepository
import com.nixops.scraper.repository.ProgramRepository
import org.springframework.transaction.annotation.Transactional

@SpringBootApplication
@RestController
class ScraperApplication(
    private val programRepository: ProgramRepository,
    private val programMapper: ProgramMapper,
    private val degreeMapper: DegreeMapper,
    private val degreeRepository: DegreeRepository
) {
    @Transactional
    @GetMapping("/hello")
    fun hello(@RequestParam(value = "name", defaultValue = "World") name: String): String {

        val programsApi = StudyProgramsApi(
            basePath = "https://api.srv.nat.tum.de"
        );

        if (false) {
            try {
                val response = programsApi.readProgramsCombined()
                for (program in response) {
                    println("Program: $program")

                    val programEntity = programMapper.toEntity(program);
                    programRepository.save(programEntity);
                }
            } catch (e: Exception) {
                println("API error: ${e.message}")
            }
        }

        try {
            val response = programsApi.readDegreeTypes()
            for (degree in response) {
                println("Degree: $degree")

                val existingDegree = degreeRepository.findById(degree.degreeTypeId.toLong()).orElse(null)
                if (existingDegree != null) {
                    println("Updating existing entity: $existingDegree")

                    existingDegree.degreeTypeName = degree.degreeTypeName
                    existingDegree.degreeTypeShort = degree.degreeTypeShort
                    existingDegree.programTypeName = degree.programTypeName
                    existingDegree.programTypeNameEn = degree.programTypeNameEn
                    degreeRepository.save(existingDegree)

                    println("Updated existing entity: $existingDegree")
                } else {
                    val degreeEntity = degreeMapper.toEntity(degree)

                    println("Saving new entity: $degreeEntity")

                    degreeRepository.save(degreeEntity)

                    println("Saved new entity: $degreeEntity")
                }
            }
        } catch (e: Exception) {
            println("API error: ${e.message}")
        }

        return "Hello $name!"
    }
}

fun main(args: Array<String>) {
    runApplication<ScraperApplication>(*args)
}
