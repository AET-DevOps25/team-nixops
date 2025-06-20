package com.nixops.scraper

import com.nixops.scraper.repository.*
import org.springframework.data.repository.findByIdOrNull

class Scraper(
    private val studyProgramRepository: StudyProgramRepository,
    private val semesterRepository: SemesterRepository,
    private val curriculumRepository: CurriculumRepository,
    private val moduleRepository: ModuleRepository,
    private val courseRepository: CourseRepository
) {
  fun getModuleById(id: Long) {
    val module = moduleRepository.findByIdOrNull(id)
    if (module != null) {
      println("got module")
    } else {
      println("fetch module")
    }
  }
}
