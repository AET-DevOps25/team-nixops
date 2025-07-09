package com.nixops.scraper.tum_api.nat.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nixops.scraper.config.ApiClientProperties
import com.nixops.scraper.tum_api.nat.model.NatSemester
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request

class NatSemesterApiClient(
    natApiClientProperties: ApiClientProperties.Nat,
    private val client: OkHttpClient = OkHttpClient()
) {
  private val mapper = jacksonObjectMapper()
  private val baseUrl: String = natApiClientProperties.baseUrl

  /** Fetch the current lecture semester. */
  @Throws(IOException::class)
  fun getCurrentLectureSemester(): NatSemester? {
    return getSemester("lecture")
  }

  @Throws(IOException::class)
  fun getSemester(semesterKey: String): NatSemester? {
    val url = "$baseUrl/semesters/$semesterKey"

    val request = Request.Builder().url(url).build()

    val response = client.newCall(request).execute()

    if (response.code == 422) return null

    if (!response.isSuccessful) {
      throw IOException("Failed to fetch semester: $response")
    }

    val body = response.body?.string() ?: throw IOException("Empty response body")

    return mapper.readValue(body)
  }

  @Throws(IOException::class)
  fun getSemesters(): List<NatSemester> {
    val url = "$baseUrl/semesters/"

    val request = Request.Builder().url(url).build()

    val response = client.newCall(request).execute()

    if (!response.isSuccessful) {
      throw IOException("Failed to fetch semester: $response")
    }

    val body = response.body?.string() ?: throw IOException("Empty response body")

    return mapper.readValue(body)
  }
}
