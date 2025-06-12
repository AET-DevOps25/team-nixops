package com.nixops.scraper.tum_api.nat.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nixops.scraper.tum_api.nat.model.NatSemester
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class NatSemesterApiClient(
    private val baseUrl: String = "https://api.srv.nat.tum.de/api/v1",
    private val client: OkHttpClient = OkHttpClient()
) {
    private val mapper = jacksonObjectMapper()

    /**
     * Fetch the current lecture semester.
     */
    fun getCurrentLectureSemester(): NatSemester {
        val url = "$baseUrl/semesters/lecture"

        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw IOException("Failed to fetch semester: $response")
        }

        val body = response.body?.string()
            ?: throw IOException("Empty response body")

        return mapper.readValue(body)
    }
}
