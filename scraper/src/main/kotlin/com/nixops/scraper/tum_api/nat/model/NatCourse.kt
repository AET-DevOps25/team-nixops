package com.nixops.scraper.tum_api.nat.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class NatCourse(
    @JsonProperty("course_id")
    val courseId: Int,

    @JsonProperty("course_code")
    val courseCode: String? = null,

    @JsonProperty("course_name")
    val courseName: String? = null,

    @JsonProperty("course_name_en")
    val courseNameEn: String? = null,

    @JsonProperty("course_name_list")
    val courseNameList: String? = null,

    @JsonProperty("course_name_list_en")
    val courseNameListEn: String? = null,

    @JsonProperty("hoursperweek")
    val hoursPerWeek: String? = null,

    @JsonProperty("semester")
    val semester: NatSemester? = null,

    @JsonProperty("modified_tumonline")
    val modifiedTumonline: String? = null,

    @JsonProperty("activity")
    val activity: Activity? = null,

    @JsonProperty("ghk")
    val ghk: Int? = null,

    @JsonProperty("instruction_languages")
    val instructionLanguages: List<String>? = null,

    @JsonProperty("description")
    val description: String? = null,

    @JsonProperty("description_en")
    val descriptionEn: String? = null,

    @JsonProperty("teachingmethod")
    val teachingMethod: String? = null,

    @JsonProperty("teachingmethod_en")
    val teachingMethodEn: String? = null,

    @JsonProperty("note")
    val note: String? = null,

    @JsonProperty("note_en")
    val noteEn: String? = null,

    @JsonProperty("tumonline_url")
    val tumonlineUrl: String? = null,

    @JsonProperty("modules")
    val modules: List<NatModule> = emptyList(),

    @JsonProperty("org")
    val org: Org? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Activity(
    @JsonProperty("activity_id")
    val activityId: String? = null,

    @JsonProperty("activity_name")
    val activityName: String? = null,

    @JsonProperty("activity_name_en")
    val activityNameEn: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Org(
    @JsonProperty("org_id")
    val orgId: Int?,
)
