package com.nixops.scraper.tum_api.nat.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class NatModule(
    @JsonProperty("module_id") val id: Int,
    @JsonProperty("module_code") val code: String,
    @JsonProperty("module_title") val title: String,
    @JsonProperty("module_title_en") val titleEn: String?,
    @JsonProperty("module_languages") val languages: Set<Language> = setOf(),
    @JsonProperty("module_content") val content: String?,
    @JsonProperty("module_content_en") val contentEn: String?,
    @JsonProperty("module_outcome") val outcome: String?,
    @JsonProperty("module_outcome_en") val outcomeEn: String?,
    @JsonProperty("module_methods") val methods: String?,
    @JsonProperty("module_methods_en") val methodsEn: String?,
    @JsonProperty("module_exam") val exam: String?,
    @JsonProperty("module_exam_en") val examEn: String?,
    @JsonProperty("module_credits") val credits: Float,
    @JsonProperty("courses") val courses: Map<String, List<NatCourse>> = mapOf()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Language(
    @JsonProperty("title") val name: String,
    @JsonProperty("title_en") val nameEn: String,
    @JsonProperty("short") val short: String?,
    @JsonProperty("short_en") val shortEn: String?
)
