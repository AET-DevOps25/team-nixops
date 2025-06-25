package com.nixops.scraper.services

import com.nixops.scraper.model.StudyProgram
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class StudyProgramService {
  fun getStudyPrograms(): List<StudyProgram> {
    return transaction {
      StudyProgram.all()
          .groupBy { it.studyId }
          .mapNotNull { (_, programs) -> programs.maxByOrNull { it.spoVersion.toIntOrNull() ?: 0 } }
    }
  }
}
