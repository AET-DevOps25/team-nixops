package com.nixops.scraper.model

import org.jetbrains.exposed.dao.id.IntIdTable

object CurriculumCourses : IntIdTable("curriculum_courses") {
  val curriculum = integer("curriculum_id")
  val semester = integer("semester_id")
  val course = integer("course_id")

  init {
    uniqueIndex(curriculum, semester, course)
  }
}
