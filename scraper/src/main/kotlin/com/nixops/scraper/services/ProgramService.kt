package com.nixops.scraper.services

import com.nixops.scraper.mapper.StudyProgramMapper
import com.nixops.scraper.model.StudyProgram
import com.nixops.scraper.repository.StudyProgramRepository
import com.nixops.scraper.tum_api.nat.api.NatProgramApiClient
import org.springframework.stereotype.Service

@Service
class ProgramService(
    private val programRepository: StudyProgramRepository,
    private val programApiClient: NatProgramApiClient,
    private val programMapper: StudyProgramMapper
) {
  fun searchPrograms(query: String): List<StudyProgram> {
    val programs = programApiClient.searchPrograms(query)
    return programs.map { programMapper.natStudyProgramToStudyProgram(it) }
  }

  fun searchProgramWithSpo(query: String, spo: String): StudyProgram? {
    val programs = searchPrograms(query)
    return programs.find { it.spoVersion == spo }
  }
}
