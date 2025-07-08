package com.nixops.scraper.model

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

object Groups : IdTable<Int>("groups") {
  override val id = integer("group_id").entityId()
  val name = varchar("name", 255)
  val courseId = reference("course_id", Courses)

  override val primaryKey = PrimaryKey(id, name = "PK_GROUP_ID")
}

class Group(id: EntityID<Int>) : Entity<Int>(id) {
  companion object : EntityClass<Int, Group>(Groups)

  var name by Groups.name
  val appointments by Appointment referrersOn Appointments.groupId
  var course by Course referencedOn Groups.courseId
}
