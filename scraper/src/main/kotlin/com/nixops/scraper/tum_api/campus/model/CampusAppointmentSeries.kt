package com.nixops.scraper.tum_api.campus.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class CampusAppointmentSeries(
    @JsonProperty("id") val id: Int,
    @JsonProperty("seriesBegin") val seriesBeginDate: DateTime,
    @JsonProperty("seriesEnd") val seriesEndDate: DateTime,
    @JsonProperty("weekday") val weekdays: List<Weekday>,
    @JsonProperty("seriesBeginTime") val beginTime: String,
    @JsonProperty("seriesEndTime") val endTime: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DateTime(
    @JsonProperty("value") val value: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Weekday(
    @JsonProperty("id") val id: Int,
    @JsonProperty("key") val key: String,
)
