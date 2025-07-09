package com.nixops.scraper.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object AppointmentWeekdays : IntIdTable("appointment_weekdays") {
  val appointment = reference("appointment_id", Appointments)
  val name = varchar("name", 20)

  init {
    uniqueIndex("uq_appointment_name", appointment, name)
  }
}

class Weekday(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<Weekday>(AppointmentWeekdays)

  var name by AppointmentWeekdays.name
  var appointment by Appointment referencedOn AppointmentWeekdays.appointment
}
