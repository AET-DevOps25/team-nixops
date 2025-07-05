package com.nixops.schedulingEngine.model

data class Response(val scheduled: Schedule, val complement: List<Event> = emptyList())
