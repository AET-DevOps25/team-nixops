package com.nixops.scraper.model

import Degree
import Degrees
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

object StudyPrograms : IdTable<Int>("study_programs") {
  override val id = integer("study_id").entityId()
  val orgId = integer("org_id")
  val spoVersion = varchar("spo_version", 255)
  val programName = varchar("program_name", 255)
  val degreeProgramName = varchar("degree_program_name", 255)
  val degree = reference("degree_id", Degrees)
}

class StudyProgram(id: EntityID<Int>) : Entity<Int>(id) {
  companion object : EntityClass<Int, StudyProgram>(StudyPrograms)

  var orgId by StudyPrograms.orgId
  var spoVersion by StudyPrograms.spoVersion
  var programName by StudyPrograms.programName
  var degreeProgramName by StudyPrograms.degreeProgramName
  var degree by Degree referencedOn StudyPrograms.degree
}
