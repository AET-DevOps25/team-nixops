package com.nixops.scraper.tum_api.nat.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nixops.scraper.config.ApiClientProperties
import com.nixops.scraper.tum_api.nat.model.NatSemester
import java.io.IOException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class NatSemesterApiClientTest {

  private val mockClient: OkHttpClient = mock()
  private val mockCall: Call = mock()
  private val objectMapper = jacksonObjectMapper()
  private val natConfig = ApiClientProperties.Nat.of("https://example.com/api")

  private val sampleSemester =
      NatSemester(
          semesterKey = "lecture",
          semesterIdTumOnline = 20251,
          semesterTag = "WS 2025/26",
          semesterTitle = "Winter Semester 2025/26")

  private fun buildSemesterResponse(semester: NatSemester): String =
      objectMapper.writeValueAsString(semester)

  private fun buildSemesterListResponse(semesters: List<NatSemester>): String =
      objectMapper.writeValueAsString(semesters)

  @Test
  fun `getCurrentLectureSemester returns semester successfully`() {
    val responseBody =
        buildSemesterResponse(sampleSemester).toResponseBody("application/json".toMediaTypeOrNull())

    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/semesters/lecture").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = NatSemesterApiClient(natConfig, mockClient)
    val result = apiClient.getCurrentLectureSemester()

    assertNotNull(result)
    assertEquals(sampleSemester, result)
  }

  @Test
  fun `getSemester returns semester successfully`() {
    val responseBody =
        buildSemesterResponse(sampleSemester).toResponseBody("application/json".toMediaTypeOrNull())

    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/semesters/lecture").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = NatSemesterApiClient(natConfig, mockClient)
    val result = apiClient.getSemester("lecture")

    assertNotNull(result)
    assertEquals(sampleSemester, result)
  }

  @Test
  fun `getSemester returns null on 422 response`() {
    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/semesters/invalid").build())
            .protocol(Protocol.HTTP_1_1)
            .code(422)
            .message("Unprocessable Entity")
            .body("".toResponseBody(null))
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = NatSemesterApiClient(natConfig, mockClient)
    val result = apiClient.getSemester("invalid")

    assertNull(result)
  }

  @Test
  fun `getSemester throws IOException on unsuccessful response`() {
    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/semesters/error").build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal Server Error")
            .body("".toResponseBody(null))
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = NatSemesterApiClient(natConfig, mockClient)

    assertThrows(IOException::class.java) { apiClient.getSemester("error") }
  }

  @Test
  fun `getSemester throws IOException on empty response body`() {
    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/semesters/empty").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(null)
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = NatSemesterApiClient(natConfig, mockClient)

    assertThrows(IOException::class.java) { apiClient.getSemester("empty") }
  }

  @Test
  fun `getSemesters returns list of semesters`() {
    val semesters =
        listOf(sampleSemester, NatSemester("lecture2", 20252, "SS 2026", "Summer Semester 2026"))

    val responseBody =
        buildSemesterListResponse(semesters).toResponseBody("application/json".toMediaTypeOrNull())

    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/semesters/").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = NatSemesterApiClient(natConfig, mockClient)
    val result = apiClient.getSemesters()

    assertNotNull(result)
    assertEquals(2, result.size)
    assertEquals(semesters, result)
  }

  @Test
  fun `getSemesters throws IOException on unsuccessful response`() {
    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/semesters/").build())
            .protocol(Protocol.HTTP_1_1)
            .code(503)
            .message("Service Unavailable")
            .body("".toResponseBody(null))
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = NatSemesterApiClient(natConfig, mockClient)

    assertThrows(IOException::class.java) { apiClient.getSemesters() }
  }

  @Test
  fun `getSemesters throws IOException on empty response body`() {
    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/semesters/").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(null)
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = NatSemesterApiClient(natConfig, mockClient)

    assertThrows(IOException::class.java) { apiClient.getSemesters() }
  }

  @Test
  @Tag("remoteApi")
  fun `remote endpoint is reachable and returns current lecture semester`() {
    val realClient = OkHttpClient()
    val realConfig = ApiClientProperties.Nat()

    val apiClient = NatSemesterApiClient(realConfig, realClient)

    val semester = apiClient.getCurrentLectureSemester()

    assertNotNull(semester, "Current lecture semester should not be null")
    assertTrue(semester?.semesterKey?.isNotEmpty() == true, "Semester key should not be empty")
  }

  @Test
  @Tag("remoteApi")
  fun `remote endpoint is reachable and returns semester by key`() {
    val realClient = OkHttpClient()
    val realConfig = ApiClientProperties.Nat()

    val apiClient = NatSemesterApiClient(realConfig, realClient)

    val semesterKey = "2024s"
    val semester = apiClient.getSemester(semesterKey)

    assertNotNull(semester, "Semester should not be null")
    assertEquals(semesterKey, semester?.semesterKey, "Semester key should match requested key")
  }

  @Test
  @Tag("remoteApi")
  fun `remote endpoint is reachable and returns list of semesters`() {
    val realClient = OkHttpClient()
    val realConfig = ApiClientProperties.Nat()

    val apiClient = NatSemesterApiClient(realConfig, realClient)

    val semesters = apiClient.getSemesters()

    assertNotNull(semesters, "Semesters list should not be null")
    assertTrue(semesters.isNotEmpty(), "Semesters list should not be empty")
  }
}
