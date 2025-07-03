package com.nixops.scraper.tum_api.campus.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nixops.scraper.config.ApiClientProperties
import com.nixops.scraper.tum_api.campus.model.CampusCourse
import com.nixops.scraper.tum_api.campus.model.CampusGroup
import java.io.IOException
import mu.KotlinLogging
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

private val logger = KotlinLogging.logger {}

class CampusCourseApiClient(
    campusApiClientProperties: ApiClientProperties.Campus,
    private val client: OkHttpClient = OkHttpClient()
) {
  private val mapper = jacksonObjectMapper()
  private val baseUrl: String = campusApiClientProperties.baseUrl

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
    val top = 50
    var totalCount: Int

    do {
      val urlBuilder =
          ("$baseUrl/slc.tm.cp/student/courses").toHttpUrlOrNull()?.newBuilder()
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

      if (totalCount > 0) {
        logger.trace("Fetched ${allCourses.size}/${totalCount} courses")
      }

      skip = allCourses.size
    } while (allCourses.size < totalCount)

    return allCourses
  }

  fun getCourseGroups(courseId: Int): List<CampusGroup>? {
    val url = "$baseUrl/slc.tm.cp/student/courseGroups/firstGroups/$courseId"

    val request = Request.Builder().url(url).addHeader("Accept", "application/json").build()

    val response = client.newCall(request).execute()

    if (response.code == 404) return null

    if (!response.isSuccessful) {
      throw IOException("Unexpected response: $response")
    }

    val body =
        response.body?.string()
            ?: throw IOException("Empty response body for groups for courseId: $courseId")

    val node = mapper.readTree(body)
    val courseGroups = node["courseGroupDtos"] ?: throw Exception("Missing 'courseGroupDtos' node")

    return mapper.readValue(courseGroups.toString())
  }
}
