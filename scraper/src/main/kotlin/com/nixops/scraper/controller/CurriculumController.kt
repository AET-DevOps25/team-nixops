package com.nixops.scraper.controller

import com.nixops.scraper.model.Curriculum
import com.nixops.scraper.services.CurriculumService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/curricula")
class CurriculumController(
    private val curriculumService: CurriculumService
) {

    @GetMapping("/{semesterKey}")
    fun getCurriculaBySemesterKey(
        @PathVariable semesterKey: String
    ): List<Curriculum>? {
        return curriculumService.getCurriculaBySemesterKey(semesterKey)
    }

    @GetMapping("/{semesterKey}/by-name")
    fun getCurriculumByProgramName(
        @PathVariable semesterKey: String,
        @RequestParam name: String
    ): Curriculum? {
        return curriculumService.getCurriculumByProgramName(semesterKey, name)
    }
}
