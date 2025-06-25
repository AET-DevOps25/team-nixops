package com.nixops.scraper.services

import com.nixops.scraper.mapper.StudyProgramMapper
import com.nixops.scraper.model.StudyProgram
import com.nixops.scraper.tum_api.nat.api.NatProgramApiClient
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class StudyProgramService(
    private val programApiClient: NatProgramApiClient,
    private val programMapper: StudyProgramMapper
) {
  fun searchPrograms(query: String): List<StudyProgram> = transaction {
    val natPrograms = programApiClient.searchPrograms(query)
    natPrograms.map { natProgram -> programMapper.natStudyProgramToStudyProgram(natProgram) }
  }

  fun searchProgramWithSpo(query: String, spo: String): StudyProgram? = transaction {
    val programs = searchPrograms(query)
    programs.find { it.spoVersion == spo }
  }
}
