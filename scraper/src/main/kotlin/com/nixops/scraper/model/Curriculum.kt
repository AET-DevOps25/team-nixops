package com.nixops.scraper.model

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Curriculums : IntIdTable("curriculums") {
  val name = varchar("curriculum_name", 255)
}

class Curriculum(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<Curriculum>(Curriculums)

  var name by Curriculums.name
}
