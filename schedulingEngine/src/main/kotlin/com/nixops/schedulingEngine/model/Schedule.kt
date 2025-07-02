package com.nixops.schedulingEngine.model

data class Schedule(
    val events: List<Event>,
    val score: Int,
    val collisions: List<Collision> = emptyList()
)
