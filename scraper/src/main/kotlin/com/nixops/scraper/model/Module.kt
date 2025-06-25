package com.nixops.scraper.model

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

object Modules : IdTable<Int>("modules") {
  override val id = integer("module_id").entityId() // manually assigned primary key
  val moduleTitle = varchar("module_title", 255).nullable()
  val moduleCode = varchar("module_code", 255).nullable()
}

class Module(id: EntityID<Int>) : Entity<Int>(id) {
  companion object : EntityClass<Int, Module>(Modules)

  var moduleTitle by Modules.moduleTitle
  var moduleCode by Modules.moduleCode
}
