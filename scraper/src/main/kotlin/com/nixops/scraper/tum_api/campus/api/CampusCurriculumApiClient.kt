package com.nixops.scraper.tum_api.campus.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nixops.scraper.tum_api.campus.model.CampusCurriculum
import okhttp3.OkHttpClient
import okhttp3.Request

class CampusCurriculumApiClient(
    private val baseUrl: String = "https://campus.tum.de/tumonline/ee/rest/slc.cm.cs.student",
    private val client: OkHttpClient = OkHttpClient()
) {
    private val mapper = jacksonObjectMapper()

    /**
     * Fetch all curricula for a given semester (e.g., "204")
     */
    fun getCurriculaForSemester(semesterId: Int): List<CampusCurriculum> {
        val url = "$baseUrl/curricula/$semesterId"
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Unexpected code $response")

            val body = response.body?.string() ?: throw Exception("Empty response body")

            val node = mapper.readTree(body)
            val resourceNode = node["resource"] ?: throw Exception("Missing 'resource' node")

            return mapper.readValue(resourceNode.toString())
        }
    }
}

