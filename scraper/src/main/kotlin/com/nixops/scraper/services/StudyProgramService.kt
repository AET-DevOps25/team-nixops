package com.nixops.scraper.services

import com.nixops.scraper.model.StudyProgram
import com.nixops.scraper.model.StudyPrograms
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class StudyProgramService {
  fun getStudyProgram(studyId: Long): StudyProgram? {
    return transaction { StudyProgram.find { StudyPrograms.studyId eq studyId }.firstOrNull() }
  }

  fun getStudyPrograms(): List<StudyProgram> {
    return transaction {
      StudyProgram.all()
          .groupBy { it.studyId }
          .mapNotNull { (_, programs) -> programs.maxByOrNull { it.spoVersion.toIntOrNull() ?: 0 } }
    }
  }
}
