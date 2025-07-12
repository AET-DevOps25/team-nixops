package com.nixops.schedulemanager.model

class Appointment(val type: String, val appointment: com.nixops.openapi.scraper.model.Appointment) {
  override fun toString(): String {
    return "Appointment(type=$type, appointment=$appointment)"
  }
}
