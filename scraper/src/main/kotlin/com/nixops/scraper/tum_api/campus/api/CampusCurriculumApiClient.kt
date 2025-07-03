package com.nixops.scraper.tum_api.campus.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nixops.scraper.config.ApiClientProperties
import com.nixops.scraper.tum_api.campus.model.CampusCurriculum
import okhttp3.OkHttpClient
import okhttp3.Request

class CampusCurriculumApiClient(
    campusApiClientProperties: ApiClientProperties.Campus,
    private val client: OkHttpClient = OkHttpClient()
) {
  private val mapper = jacksonObjectMapper()
  private val baseUrl: String = campusApiClientProperties.baseUrl

  /** Fetch all curricula for a given semester (e.g., "204") */
  fun getCurriculaForSemester(semesterId: Int): List<CampusCurriculum> {
    val url = "$baseUrl/slc.cm.cs.student/curricula/$semesterId"
    val request = Request.Builder().url(url).addHeader("Accept", "application/json").build()

    val response = client.newCall(request).execute()

    if (!response.isSuccessful) throw Exception("Unexpected code $response")

    val body = response.body?.string() ?: throw Exception("Empty response body")

    val node = mapper.readTree(body)
    val resourceNode = node["resource"] ?: throw Exception("Missing 'resource' node")

    return mapper.readValue(resourceNode.toString())
  }
}
