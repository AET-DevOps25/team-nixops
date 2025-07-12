package com.nixops.schedulemanager.model

data class Schedule(
    val scheduleId: String,
    val studyId: Long,
    val semester: String,
    val modules: MutableSet<Module>
)
