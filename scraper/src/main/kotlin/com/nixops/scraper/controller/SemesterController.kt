package com.nixops.scraper.controller

import com.nixops.scraper.model.Semester
import com.nixops.scraper.services.SemesterService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/semester")
class SemesterController(
    private val semesterService: SemesterService
) {
    @GetMapping("lecture")
    fun getCurrentLectureSemester(): Semester {
        return semesterService.getCurrentLectureSemester()
    }

    @GetMapping("{semesterKey}")
    fun getSemester(@PathVariable semesterKey: String): Semester {
        return semesterService.getSemester(semesterKey)
    }

    @GetMapping
    fun getSemesters(): List<Semester> {
        return semesterService.getSemesters()
    }
}