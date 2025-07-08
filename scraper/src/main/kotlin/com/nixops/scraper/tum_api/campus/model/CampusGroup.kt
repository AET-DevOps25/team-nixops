package com.nixops.scraper.tum_api.campus.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class CampusGroup(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("appointmentSeriesDtos")
    val appointments: List<CampusAppointmentSeries> = listOf()
)
