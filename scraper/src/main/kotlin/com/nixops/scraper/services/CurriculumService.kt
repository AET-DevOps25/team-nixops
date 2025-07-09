package com.nixops.scraper.services

import com.nixops.scraper.model.Curriculum
import com.nixops.scraper.model.Curriculums
import com.nixops.scraper.model.Semester
import com.nixops.scraper.model.StudyProgram
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CurriculumService {
  fun getCurriculum(studyProgram: StudyProgram, semester: Semester): Curriculum? {
    return transaction { Curriculum.find(Curriculums.name eq studyProgram.fullName).firstOrNull() }
  }
}
