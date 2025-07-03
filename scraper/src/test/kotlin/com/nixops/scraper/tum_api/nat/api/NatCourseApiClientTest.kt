package com.nixops.scraper.tum_api.nat.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nixops.scraper.config.ApiClientProperties
import com.nixops.scraper.tum_api.nat.model.*
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NatCourseApiClientTest {

  private val mockClient: OkHttpClient = mockk()
  private val mockCall: Call = mockk()
  private val objectMapper = jacksonObjectMapper()

  private val courseId = 123
  private val natConfig = ApiClientProperties.Nat.of("https://example.com/api")

  private val course =
      NatCourse(
          courseId = 1,
          courseCode = "CS101",
          courseName = "Introduction to Computer Science",
          courseNameEn = "Intro to CS",
          hoursPerWeek = "4",
          semester = null,
          modifiedTumonline = "2024-01-01T00:00:00",
          activity =
              Activity(activityId = "A1", activityName = "Lecture", activityNameEn = "Lecture"),
          ghk = 5,
          instructionLanguages = listOf("English", "German"),
          description = "Course description",
          descriptionEn = "Course description in English",
          teachingMethod = "In-person",
          teachingMethodEn = "In-person",
          note = "Note about course",
          noteEn = "Note in English",
          tumonlineUrl = "http://tumonline.com/cs101",
          modules = emptyList(),
          org = Org(orgId = 123),
          groups =
              listOf(
                  NatGroup(
                      groupId = 10,
                      groupName = "Group A",
                      events =
                          listOf(
                              NatEvent(
                                  eventId = 100,
                                  start = "2025-01-10T10:00:00",
                                  end = "2025-01-10T12:00:00",
                                  type =
                                      NatEventType(
                                          eventTypeId = "E1",
                                          eventType = "Lecture",
                                          eventTypeEn = "Lecture"))))))

  @Test
  fun `should return course when response is successful`() {
    val jsonResponse = objectMapper.writeValueAsString(course)
    val responseBody = jsonResponse.toResponseBody("application/json".toMediaTypeOrNull())
    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/course/$courseId").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .build()

    every { mockClient.newCall(any()) } returns mockCall
    every { mockCall.execute() } returns response

    val apiClient = NatCourseApiClient(natConfig, mockClient)
    val result = apiClient.getCourseById(courseId)

    assertNotNull(result)
    assertEquals(course, result)
  }

  @Test
  fun `should return null when response is 404`() {
    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/course/$courseId").build())
            .protocol(Protocol.HTTP_1_1)
            .code(404)
            .message("Not Found")
            .body("".toResponseBody(null))
            .build()

    every { mockClient.newCall(any()) } returns mockCall
    every { mockCall.execute() } returns response

    val apiClient = NatCourseApiClient(natConfig, mockClient)
    val result = apiClient.getCourseById(courseId)

    assertNull(result)
  }

  @Test
  fun `should throw IOException when response is unsuccessful`() {
    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/course/$courseId").build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal Server Error")
            .body("".toResponseBody(null))
            .build()

    every { mockClient.newCall(any()) } returns mockCall
    every { mockCall.execute() } returns response

    val apiClient = NatCourseApiClient(natConfig, mockClient)

    assertThrows(IOException::class.java) { apiClient.getCourseById(courseId) }
  }

  @Test
  @Tag("remoteApi")
  fun `remote endpoint is reachable and returns parsable Nat course`() {
    val realClient = OkHttpClient()
    val realConfig = ApiClientProperties.Nat()

    val apiClient = NatCourseApiClient(realConfig, realClient)

    val realCourseId = 950798660

    val course = apiClient.getCourseById(realCourseId)

    assertNotNull(course, "Course should not be null")
    assertEquals(realCourseId, course?.courseId, "Course ID should match requested ID")
  }
}
