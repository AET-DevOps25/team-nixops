package com.nixops.schedulemanager.model

class Module(
    val module: com.nixops.openapi.scraper.model.Module,
    val appointments: List<Appointment>
) {
  override fun equals(other: Any?): Boolean {
    return other is Module && this.module.code == other.module.code
  }

  override fun hashCode(): Int {
    return this.module.code.hashCode()
  }
}
