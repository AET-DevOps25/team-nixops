package com.nixops.scraper.tum_api.campus.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nixops.scraper.tum_api.campus.model.CampusCourse
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

class CampusCourseApiClient(
    private val baseUrl: String = "https://campus.tum.de/tumonline/ee/rest/slc.tm.cp/student",
    private val client: OkHttpClient = OkHttpClient()
) {
  private val mapper = jacksonObjectMapper()

  @JsonIgnoreProperties(ignoreUnknown = true)
  data class CoursesResponse(
      @JsonProperty("courses") val courses: List<CampusCourse>,
      @JsonProperty("totalCount") val totalCount: Int
  )

  /**
   * Fetch all courses matching the filter criteria with pagination.
   *
   * @param courseNormKey The course norm key, e.g. "LVEAB"
   * @param curriculumVersionId Curriculum ID to filter by
   * @param orgId Organization ID to filter by
   * @param termId Term/Semester ID to filter by
   */
  fun getCourses(curriculumVersionId: Int, termId: Int): List<CampusCourse> {
    val allCourses = mutableListOf<CampusCourse>()
    var skip = 0
    val top = 20
    var totalCount: Int

    do {
      val urlBuilder =
          ("$baseUrl/courses").toHttpUrlOrNull()?.newBuilder()
              ?: throw IllegalArgumentException("Invalid base URL")

      val filterValue =
          "courseNormKey-eq=LVEAB;curriculumVersionId-eq=$curriculumVersionId;orgId-eq=1;termId-eq=$termId"

      urlBuilder.addQueryParameter("\$filter", filterValue)
      urlBuilder.addQueryParameter("\$skip", skip.toString())
      urlBuilder.addQueryParameter("\$top", top.toString())

      val request =
          Request.Builder().url(urlBuilder.build()).addHeader("Accept", "application/json").build()

      client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
          throw Exception("Unexpected response $response")
        }

        val body = response.body?.string() ?: throw Exception("Empty response body")
        val coursesResponse = mapper.readValue<CoursesResponse>(body)

        allCourses.addAll(coursesResponse.courses)
        totalCount = coursesResponse.totalCount
      }

      println("Fetched ${allCourses.size}/${totalCount} courses")

      skip = allCourses.size
    } while (allCourses.size < totalCount)

    return allCourses
  }
}
