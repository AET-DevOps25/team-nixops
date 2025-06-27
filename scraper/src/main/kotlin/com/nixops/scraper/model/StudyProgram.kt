package com.nixops.scraper.model

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object StudyPrograms : IntIdTable("study_programs") {
  val studyId = long("study_id")
  val orgId = integer("org_id")
  val spoVersion = varchar("spo_version", 255)
  val programName = varchar("program_name", 255)
  val degreeProgramName = varchar("degree_program_name", 255)
  val degreeTypeName = varchar("degree_type_name", 255)

  init {
    uniqueIndex("uq_study_id_spo_version", studyId, spoVersion)
  }
}

class StudyProgram(id: EntityID<Int>) : Entity<Int>(id) {
  companion object : EntityClass<Int, StudyProgram>(StudyPrograms)

  var studyId by StudyPrograms.studyId
  var orgId by StudyPrograms.orgId
  var spoVersion by StudyPrograms.spoVersion
  var programName by StudyPrograms.programName
  var degreeProgramName by StudyPrograms.degreeProgramName
  var degreeTypeName by StudyPrograms.degreeTypeName

  fun fullName(): String {
    return "${this.programName} [${this.spoVersion}], ${this.degreeTypeName}"
  }
}
