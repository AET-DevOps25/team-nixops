package com.nixops.scraper.tum_api.campus.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nixops.scraper.config.ApiClientProperties
import com.nixops.scraper.tum_api.campus.model.*
import io.mockk.*
import java.io.IOException
import okhttp3.*
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CampusCourseApiClientTest {

  private val mockClient: OkHttpClient = mockk()
  private val mockCall: Call = mockk()
  private val mapper = jacksonObjectMapper()
  private val campusConfig = ApiClientProperties.Campus.of("https://example.com/api")

  private val sampleCourse =
      CampusCourse(
          id = 1,
          courseTitle = com.nixops.scraper.tum_api.campus.model.LangData("Sample Course"),
          semesterDto = com.nixops.scraper.tum_api.campus.model.SemesterDto(123))

  private val sampleAppointments =
      listOf(
          CampusAppointmentSeries(
              id = 1,
              seriesBeginDate = DateTime("2025-09-01"),
              seriesEndDate = DateTime("2025-12-15"),
              weekdays = listOf(Weekday(1, "MON"), Weekday(3, "WED")),
              beginTime = "10:00",
              endTime = "11:30"),
          CampusAppointmentSeries(
              id = 2,
              seriesBeginDate = DateTime("2025-09-02"),
              seriesEndDate = DateTime("2025-12-16"),
              weekdays = listOf(Weekday(2, "TUE")),
              beginTime = "14:00",
              endTime = "15:30"))

  private val sampleGroup =
      CampusGroup(id = 100, name = "Group A", appointments = sampleAppointments)

  private fun buildCoursesResponse(courses: List<CampusCourse>, totalCount: Int): String {
    val node = mapOf("courses" to courses, "totalCount" to totalCount)
    return mapper.writeValueAsString(node)
  }

  private fun buildGroupsResponse(groups: List<CampusGroup>): String {
    val node = mapOf("courseGroupDtos" to groups)
    return mapper.writeValueAsString(node)
  }

  @Test
  fun `getCourses returns all courses with pagination`() {
    val firstBatch = listOf(sampleCourse)
    val secondBatch =
        listOf(
            sampleCourse.copy(
                id = 2, courseTitle = sampleCourse.courseTitle.copy(value = "Course 2")))
    val totalCount = 2

    val firstResponse =
        Response.Builder()
            .request(
                Request.Builder()
                    .url(
                        "${campusConfig.baseUrl}/slc.tm.cp/student/courses?\$filter=courseNormKey-eq=LVEAB;curriculumVersionId-eq=42;orgId-eq=1;termId-eq=99&\$skip=0&\$top=50")
                    .build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(
                buildCoursesResponse(firstBatch, totalCount)
                    .toResponseBody("application/json".toMediaTypeOrNull()))
            .build()

    val secondResponse =
        Response.Builder()
            .request(
                Request.Builder()
                    .url(
                        "${campusConfig.baseUrl}/slc.tm.cp/student/courses?\$filter=courseNormKey-eq=LVEAB;curriculumVersionId-eq=42;orgId-eq=1;termId-eq=99&\$skip=1&\$top=50")
                    .build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(
                buildCoursesResponse(secondBatch, totalCount)
                    .toResponseBody("application/json".toMediaTypeOrNull()))
            .build()

    every { mockClient.newCall(any()) } returns mockCall
    every { mockCall.execute() } returnsMany listOf(firstResponse, secondResponse)

    val apiClient = CampusCourseApiClient(campusConfig, mockClient)
    val courses = apiClient.getCourses(curriculumVersionId = 42, termId = 99)

    assertEquals(totalCount, courses.size)
    assertTrue(courses.containsAll(firstBatch + secondBatch))
  }

  @Test
  fun `getCourses throws Exception on unsuccessful response`() {
    val response =
        Response.Builder()
            .request(
                Request.Builder().url("${campusConfig.baseUrl}/slc.tm.cp/student/courses").build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Server error")
            .body("".toResponseBody(null))
            .build()

    every { mockClient.newCall(any()) } returns mockCall
    every { mockCall.execute() } returns response

    val apiClient = CampusCourseApiClient(campusConfig, mockClient)

    assertThrows(Exception::class.java) {
      apiClient.getCourses(curriculumVersionId = 1, termId = 1)
    }
  }

  @Test
  fun `getCourses throws Exception on empty response body`() {
    val response =
        Response.Builder()
            .request(
                Request.Builder().url("${campusConfig.baseUrl}/slc.tm.cp/student/courses").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(null)
            .build()

    every { mockClient.newCall(any()) } returns mockCall
    every { mockCall.execute() } returns response

    val apiClient = CampusCourseApiClient(campusConfig, mockClient)

    assertThrows(Exception::class.java) {
      apiClient.getCourses(curriculumVersionId = 1, termId = 1)
    }
  }

  @Test
  fun `getCourseGroups returns list of groups`() {
    val groups = listOf(sampleGroup)
    val responseBody =
        buildGroupsResponse(groups).toResponseBody("application/json".toMediaTypeOrNull())

    val response =
        Response.Builder()
            .request(
                Request.Builder()
                    .url("${campusConfig.baseUrl}/slc.tm.cp/student/courseGroups/firstGroups/123")
                    .build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .build()

    every { mockClient.newCall(any()) } returns mockCall
    every { mockCall.execute() } returns response

    val apiClient = CampusCourseApiClient(campusConfig, mockClient)
    val result = apiClient.getCourseGroups(123)

    assertNotNull(result)
    assertEquals(groups, result)
  }

  @Test
  fun `getCourseGroups returns null on 404`() {
    val response =
        Response.Builder()
            .request(
                Request.Builder()
                    .url("${campusConfig.baseUrl}/slc.tm.cp/student/courseGroups/firstGroups/999")
                    .build())
            .protocol(Protocol.HTTP_1_1)
            .code(404)
            .message("Not found")
            .body("".toResponseBody(null))
            .build()

    every { mockClient.newCall(any()) } returns mockCall
    every { mockCall.execute() } returns response

    val apiClient = CampusCourseApiClient(campusConfig, mockClient)
    val result = apiClient.getCourseGroups(999)

    assertNull(result)
  }

  @Test
  fun `getCourseGroups throws IOException on empty response body`() {
    val response =
        Response.Builder()
            .request(
                Request.Builder()
                    .url("${campusConfig.baseUrl}/slc.tm.cp/student/courseGroups/firstGroups/456")
                    .build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(null)
            .build()

    every { mockClient.newCall(any()) } returns mockCall
    every { mockCall.execute() } returns response

    val apiClient = CampusCourseApiClient(campusConfig, mockClient)

    assertThrows(IOException::class.java) { apiClient.getCourseGroups(456) }
  }

  @Test
  @Tag("remoteApi")
  fun `remote endpoint is reachable and returns parsable courses`() {
    val realClient = OkHttpClient()
    val realConfig = ApiClientProperties.Campus()

    val apiClient = CampusCourseApiClient(realConfig, realClient)

    val courses = apiClient.getCourses(curriculumVersionId = 5217, termId = 204)

    // Basic assertions:
    assertNotNull(courses, "Courses should not be null")
    assertTrue(courses.isNotEmpty(), "Courses list should not be empty")
  }

  @Test
  @Tag("remoteApi")
  fun `remote endpoint is reachable and returns parsable course groups`() {
    val realClient = OkHttpClient()
    val realConfig = ApiClientProperties.Campus()

    val apiClient = CampusCourseApiClient(realConfig, realClient)

    val courseId = 950798660

    val groups = apiClient.getCourseGroups(courseId)

    // Basic assertions:
    assertNotNull(groups, "Course groups should not be null")
    assertFalse(groups.isNullOrEmpty(), "Course groups list should not be empty")
  }
}
