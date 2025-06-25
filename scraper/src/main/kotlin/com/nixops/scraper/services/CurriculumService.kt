package com.nixops.scraper.services

import com.nixops.scraper.mapper.CurriculumMapper
import com.nixops.scraper.model.Curriculum
import com.nixops.scraper.tum_api.campus.api.CampusCurriculumApiClient
import com.nixops.scraper.tum_api.nat.api.NatSemesterApiClient
import org.springframework.stereotype.Service

@Service
class CurriculumService(
    private val curriculumApiClient: CampusCurriculumApiClient,
    private val curriculumMapper: CurriculumMapper,
    private val semesterApiClient: NatSemesterApiClient,
) {
  fun getCurriculaBySemesterKey(semesterKey: String): List<Curriculum> {
    val semester = semesterApiClient.getSemester(semesterKey)
    val curricula =
        semester.semesterIdTumOnline?.let { tumId ->
          curriculumApiClient.getCurriculaForSemester(tumId).map {
            curriculumMapper.natCurriculumToCurriculum(it)
          }
        } ?: emptyList()

    return curricula
  }

  fun getCurriculumByProgramName(semesterKey: String, name: String): Curriculum? {
    val curricula = getCurriculaBySemesterKey(semesterKey)
    return curricula.find { it.name == name }
  }
}
