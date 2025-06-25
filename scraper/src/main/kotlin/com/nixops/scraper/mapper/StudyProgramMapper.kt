package com.nixops.scraper.mapper

import com.nixops.scraper.model.StudyProgram
import com.nixops.scraper.tum_api.nat.model.NatProgram
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Component

@Component
class StudyProgramMapper {
  fun natStudyProgramToStudyProgram(natProgram: NatProgram): StudyProgram = transaction {
    val degreeEntity =
        Degree.find { Degrees.degreeTypeName eq natProgram.degree.degreeTypeName }.firstOrNull()
            ?: Degree.new { degreeTypeName = natProgram.degree.degreeTypeName }

    val existing = StudyProgram.findById(natProgram.studyId)
    existing?.apply {
      orgId = natProgram.orgId
      spoVersion = natProgram.spoVersion
      programName = natProgram.programName
      degreeProgramName = natProgram.degreeProgramName
      degree = degreeEntity
    }
        ?: StudyProgram.new(natProgram.studyId) {
          orgId = natProgram.orgId
          spoVersion = natProgram.spoVersion
          programName = natProgram.programName
          degreeProgramName = natProgram.degreeProgramName
          degree = degreeEntity
        }
  }
}
