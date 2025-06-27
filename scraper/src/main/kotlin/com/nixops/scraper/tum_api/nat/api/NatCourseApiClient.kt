package com.nixops.scraper.tum_api.nat.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nixops.scraper.tum_api.nat.model.NatCourse
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request

class NatCourseApiClient(
    private val baseUrl: String = "https://api.srv.nat.tum.de/api/v1",
    private val client: OkHttpClient = OkHttpClient()
) {
  private val mapper = jacksonObjectMapper()

  @Throws(IOException::class)
  fun getCourseById(courseId: Int): NatCourse? {
    val url = "$baseUrl/course/$courseId"

    val request = Request.Builder().url(url).header("Accept", "application/json").build()

    val response = client.newCall(request).execute()

    if (response.code == 404) return null

    if (!response.isSuccessful) {
      throw IOException("Unexpected response: $response")
    }

    val body =
        response.body?.string() ?: throw IOException("Empty response body for courseId: $courseId")

    return mapper.readValue(body)
  }
}
