package com.nixops.scraper.model

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

object Semesters : IdTable<String>("semesters") {
  override val id = varchar("semester_key", 255).entityId()
  val semesterTag = varchar("semester_tag", 255)
  val semesterTitle = varchar("semester_title", 255)
  val semesterIdTumOnline = integer("semester_id_tumonline")

  override val primaryKey = PrimaryKey(id, name = "PK_SEMESTER_ID")
}

class Semester(id: EntityID<String>) : Entity<String>(id) {
  companion object : EntityClass<String, Semester>(Semesters)

  var semesterTag by Semesters.semesterTag
  var semesterTitle by Semesters.semesterTitle
  var semesterIdTumOnline by Semesters.semesterIdTumOnline
}
