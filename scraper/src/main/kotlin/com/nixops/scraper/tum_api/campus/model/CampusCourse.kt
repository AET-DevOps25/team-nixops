package com.nixops.scraper.tum_api.campus.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class CampusCourse(
    @JsonProperty("id") val id: Int,
    @JsonProperty("courseTitle") val courseTitle: LangData?,
    @JsonProperty("semesterDto") val semesterDto: SemesterDto?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LangData(@JsonProperty("value") val value: String?)

@JsonIgnoreProperties(ignoreUnknown = true) data class SemesterDto(@JsonProperty("id") val id: Int)
