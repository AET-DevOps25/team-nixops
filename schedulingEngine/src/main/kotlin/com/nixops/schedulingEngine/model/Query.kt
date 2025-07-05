package com.nixops.schedulingEngine.model

data class Query(
    val courses: List<Course>,
    val availability: List<AvailabilityBlocker> = emptyList(),
    val ectsTarget: Int
)
