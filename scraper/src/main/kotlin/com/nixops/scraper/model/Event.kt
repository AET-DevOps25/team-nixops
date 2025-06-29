package com.nixops.scraper.model

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

object Events : IdTable<Int>("events") {
  override val id = integer("event_id").entityId()
  val start = varchar("start", 255)
  val end = varchar("end", 255)
  val type = varchar("event_type", 255)
  val groupId = reference("group_id", Groups)
}

class Event(id: EntityID<Int>) : Entity<Int>(id) {
  companion object : EntityClass<Int, Event>(Events)

  var start by Events.start
  var end by Events.end
  var type by Events.type
  var group by Group referencedOn Events.groupId
}
