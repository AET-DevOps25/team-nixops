package com.nixops.scraper.mapper

import com.nixops.scraper.model.Course
import com.nixops.scraper.model.Module
import com.nixops.scraper.model.StudyProgram
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class StudyProgramMapper(private val moduleMapper: ModuleMapper) {
  fun studyProgramToApiStudyProgram(
      studyProgram: StudyProgram,
      semesters: Map<String, List<Pair<Module, Set<Course>>>> = mapOf()
  ): com.nixops.openapi.model.StudyProgram {
    return transaction {
      com.nixops.openapi.model.StudyProgram(
          studyId = studyProgram.studyId,
          programName = studyProgram.programName,
          degreeProgramName = studyProgram.degreeProgramName,
          degreeTypeName = studyProgram.degreeTypeName,
          semesters =
              semesters.mapValues { entry ->
                entry.value.map { pair -> moduleMapper.moduleToApiModule(pair.first, pair.second) }
              })
    }
  }
}
