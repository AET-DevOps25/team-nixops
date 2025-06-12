package com.nixops.scraper.tum_api.campus.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter

@JsonIgnoreProperties(ignoreUnknown = true)
data class CampusCurriculum(
    var id: Int,
    var name: String = ""
) {
    @JsonSetter("content")
    fun setContent(contentObj: Map<String, Any>) {
        val cmCurriculumDto = contentObj["cmCurriculumVersionDto"] as? Map<*, *>
        if (cmCurriculumDto != null) {
            this.id = (cmCurriculumDto["id"] as? Int) ?: 0

            val nameObj = cmCurriculumDto["name"] as? Map<*, *>
            this.name = nameObj?.get("value") as? String ?: ""
        }
    }
}