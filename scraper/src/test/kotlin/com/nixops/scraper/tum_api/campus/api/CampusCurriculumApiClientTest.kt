package com.nixops.scraper.tum_api.campus.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nixops.scraper.config.ApiClientProperties
import com.nixops.scraper.tum_api.campus.model.CampusCurriculum
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class CampusCurriculumApiClientTest {

  private val mockClient: OkHttpClient = mock()
  private val mockCall: Call = mock()
  private val mapper = jacksonObjectMapper()
  private val campusConfig = ApiClientProperties.Campus.of("https://example.com/api")

  private fun buildCurriculaResponse(curricula: List<CampusCurriculum>): String {
    val node = mapOf("resource" to curricula)
    return mapper.writeValueAsString(node)
  }

  @Test
  fun `getCurriculaForSemester returns list of curricula`() {
    val curricula =
        listOf(
            CampusCurriculum(id = 1, name = "Curriculum A"),
            CampusCurriculum(id = 2, name = "Curriculum B"))
    val responseBody =
        buildCurriculaResponse(curricula).toResponseBody("application/json".toMediaTypeOrNull())

    val response =
        Response.Builder()
            .request(
                Request.Builder()
                    .url("${campusConfig.baseUrl}/slc.cm.cs.student/curricula/204")
                    .build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = CampusCurriculumApiClient(campusConfig, mockClient)
    val result = apiClient.getCurriculaForSemester(204)

    assertEquals(curricula.size, result.size)
    assertTrue(result.any { it.name == "Curriculum A" })
    assertTrue(result.any { it.name == "Curriculum B" })
  }

  @Test
  fun `getCurriculaForSemester throws Exception on unsuccessful response`() {
    val response =
        Response.Builder()
            .request(
                Request.Builder()
                    .url("${campusConfig.baseUrl}/slc.cm.cs.student/curricula/204")
                    .build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Server Error")
            .body("".toResponseBody(null))
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = CampusCurriculumApiClient(campusConfig, mockClient)

    val ex = assertThrows(Exception::class.java) { apiClient.getCurriculaForSemester(204) }
    assertTrue(ex.message?.contains("Unexpected code") == true)
  }

  @Test
  fun `getCurriculaForSemester throws Exception on empty response body`() {
    val response =
        Response.Builder()
            .request(
                Request.Builder()
                    .url("${campusConfig.baseUrl}/slc.cm.cs.student/curricula/204")
                    .build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(null)
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = CampusCurriculumApiClient(campusConfig, mockClient)

    val ex = assertThrows(Exception::class.java) { apiClient.getCurriculaForSemester(204) }
    assertTrue(ex.message?.contains("Empty response body") == true)
  }

  @Test
  fun `getCurriculaForSemester throws Exception when resource node missing`() {
    val responseBody =
        """{"noResource":[]}""".toResponseBody("application/json".toMediaTypeOrNull())

    val response =
        Response.Builder()
            .request(
                Request.Builder()
                    .url("${campusConfig.baseUrl}/slc.cm.cs.student/curricula/204")
                    .build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = CampusCurriculumApiClient(campusConfig, mockClient)

    val ex = assertThrows(Exception::class.java) { apiClient.getCurriculaForSemester(204) }
    assertTrue(ex.message?.contains("Missing 'resource' node") == true)
  }
}
