package com.nixops.scraper.tum_api.nat.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class NatSemester(
    @JsonProperty("semester_key") val semesterKey: String,
    @JsonProperty("semester_id_tumonline") val semesterIdTumOnline: Int,
    @JsonProperty("semester_tag") val semesterTag: String,
    @JsonProperty("semester_title") val semesterTitle: String
)
