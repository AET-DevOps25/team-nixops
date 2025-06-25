package com.nixops.scraper.tum_api.nat.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class NatModule(
    @JsonProperty("module_id") val moduleId: Int,
    @JsonProperty("module_title") val moduleTitle: String,
    @JsonProperty("module_code") val moduleCode: String,
    @JsonProperty("courses") val courses: Map<String, List<NatCourse>>? = null
)
