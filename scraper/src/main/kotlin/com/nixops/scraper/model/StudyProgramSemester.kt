package com.nixops.scraper.model

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object StudyProgramSemester : IntIdTable("study_program_embedding") {
  val studyProgram = long("study_program_id")
  val semester = varchar("semester_key", 255)

  val last_checked = datetime("last_checked")
  val last_embedded = datetime("last_embedded")

  init {
    uniqueIndex(studyProgram, semester)
  }
}
