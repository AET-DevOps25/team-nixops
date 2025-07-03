package com.nixops.scraper.model

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

object Appointments : IdTable<Int>("appointments") {
  override val id = integer("appointment_id").entityId()
  val seriesBeginDate = varchar("series_begin_date", 20)
  val seriesEndDate = varchar("series_end_date", 20)
  val beginTime = varchar("begin_time", 50)
  val endTime = varchar("end_time", 50)
  val groupId = reference("group_id", Groups)

  override val primaryKey = PrimaryKey(Appointments.id, name = "PK_APPOINTMENT_ID")
}

class Appointment(id: EntityID<Int>) : Entity<Int>(id) {
  companion object : EntityClass<Int, Appointment>(Appointments)

  var seriesBeginDate by Appointments.seriesBeginDate
  var seriesEndDate by Appointments.seriesEndDate
  var beginTime by Appointments.beginTime
  var endTime by Appointments.endTime

  val weekdays by Weekday referrersOn AppointmentWeekdays.appointment
  var group by Group referencedOn Appointments.groupId
}
