package com.nixops.scraper.tum_api.nat.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nixops.scraper.config.ApiClientProperties
import com.nixops.scraper.tum_api.nat.model.NatProgram
import java.io.IOException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

class NatProgramApiClient(
    natApiClientProperties: ApiClientProperties.Nat,
    private val client: OkHttpClient = OkHttpClient()
) {
  private val mapper = jacksonObjectMapper()
  private val baseUrl: String = natApiClientProperties.baseUrl

  fun getPrograms(): List<NatProgram> {
    return searchPrograms("")
  }

  /** Search programs by query string, returns all paged results combined. */
  @Throws(IOException::class)
  fun searchPrograms(query: String): List<NatProgram> {
    val items = mutableListOf<NatProgram>()
    var offset: Int? = null

    do {
      val urlBuilder = "$baseUrl/programs/search".toHttpUrlOrNull()!!.newBuilder()
      urlBuilder.addQueryParameter("q", query)
      if (offset != null) {
        urlBuilder.addQueryParameter("offset", offset.toString())
      }
      val url = urlBuilder.build().toString()

      val request = Request.Builder().url(url).build()

      client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
          throw IOException("Failed to search programs: $response")
        }

        val body = response.body?.string() ?: throw IOException("Empty response body")

        val pagedResponse: PagedResponse<NatProgram> = mapper.readValue(body)
        items.addAll(pagedResponse.hits)

        offset = pagedResponse.nextOffset
      }
    } while (offset != null)

    return items
  }
}
