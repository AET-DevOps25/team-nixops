package com.nixops.scraper.tum_api.nat.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class NatModule(
    @JsonProperty("module_id") val moduleId: Int? = null,
    @JsonProperty("module_title") val moduleTitle: String? = null,
    @JsonProperty("module_code") val moduleCode: String? = null,
    @JsonProperty("courses") val courses: Map<String, List<NatCourse>>? = null
)
