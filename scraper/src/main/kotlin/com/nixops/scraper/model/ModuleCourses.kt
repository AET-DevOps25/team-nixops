package com.nixops.scraper.model

import org.jetbrains.exposed.dao.id.IntIdTable

object ModuleCourses : IntIdTable("module_courses") {
  val semester = varchar("semester_key", 255)
  val module = integer("module_id")
  val course = integer("course_id")

  init {
    uniqueIndex(semester, module, course)
  }
}
