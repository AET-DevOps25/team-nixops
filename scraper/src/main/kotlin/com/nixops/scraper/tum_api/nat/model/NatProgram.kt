package com.nixops.scraper.tum_api.nat.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class NatProgram(
    @JsonProperty("study_id")
    val studyId: Int,

    @JsonProperty("org_id")
    val orgId: Int,

    @JsonProperty("school")
    val school: NatSchool,

    @JsonProperty("spo_version")
    val spoVersion: String,

    @JsonProperty("program_name")
    val programName: String,

    @JsonProperty("degree")
    val degree: NatDegree,

    @JsonProperty("degree_program_name")
    val degreeProgramName: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NatSchool(
    @JsonProperty("org_id") val orgId: Int
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NatDegree(
    @JsonProperty("degree_type_name") val degreeTypeName: String
)