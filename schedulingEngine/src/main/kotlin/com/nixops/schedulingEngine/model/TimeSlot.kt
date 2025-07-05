package com.nixops.schedulingEngine.model

import java.time.LocalDate
import java.time.LocalTime

data class TimeSlot(val date: LocalDate, val startTime: LocalTime, val endTime: LocalTime)
