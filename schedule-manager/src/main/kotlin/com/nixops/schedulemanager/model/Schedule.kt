package com.nixops.schedulemanager.model

data class Schedule(val scheduleId: String, var semester: String, val modules: MutableSet<Module>)
