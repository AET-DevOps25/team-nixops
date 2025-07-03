package com.nixops.scraper.tum_api.nat.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nixops.scraper.config.ApiClientProperties
import com.nixops.scraper.tum_api.nat.model.NatDegree
import com.nixops.scraper.tum_api.nat.model.NatProgram
import com.nixops.scraper.tum_api.nat.model.NatSchool
import java.io.IOException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class NatProgramApiClientTest {

  private val mockClient: OkHttpClient = mock()
  private val mockCall: Call = mock()
  private val objectMapper = jacksonObjectMapper()
  private val natConfig = ApiClientProperties.Nat.of("https://example.com/api")

  private val sampleProgramsPage1 =
      listOf(
          NatProgram(
              studyId = 1L,
              orgId = 100,
              school = NatSchool(orgId = 10),
              spoVersion = "v1",
              programName = "Program One",
              degree = NatDegree("Bachelor"),
              degreeProgramName = "Bachelor of Science"))

  private val sampleProgramsPage2 =
      listOf(
          NatProgram(
              studyId = 2L,
              orgId = 101,
              school = NatSchool(orgId = 11),
              spoVersion = "v2",
              programName = "Program Two",
              degree = NatDegree("Master"),
              degreeProgramName = "Master of Science"))

  private fun buildPagedResponse(
      programs: List<NatProgram>,
      count: Int,
      totalCount: Int,
      offset: Int,
      nextOffset: Int?
  ): String {
    val pagedResponse =
        PagedResponse(
            hits = programs,
            count = count,
            totalCount = totalCount,
            offset = offset,
            nextOffset = nextOffset)
    return objectMapper.writeValueAsString(pagedResponse)
  }

  @Test
  fun `searchPrograms returns all programs across pages`() {
    // Page 1 response
    val responsePage1 =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/programs/search?q=test").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(
                buildPagedResponse(
                        programs = sampleProgramsPage1,
                        count = sampleProgramsPage1.size,
                        totalCount = 2,
                        offset = 0,
                        nextOffset = 1)
                    .toResponseBody("application/json".toMediaTypeOrNull()))
            .build()

    // Page 2 response
    val responsePage2 =
        Response.Builder()
            .request(
                Request.Builder()
                    .url("${natConfig.baseUrl}/programs/search?q=test&offset=1")
                    .build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(
                buildPagedResponse(
                        programs = sampleProgramsPage2,
                        count = sampleProgramsPage2.size,
                        totalCount = 2,
                        offset = 1,
                        nextOffset = null)
                    .toResponseBody("application/json".toMediaTypeOrNull()))
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(responsePage1).thenReturn(responsePage2)

    val apiClient = NatProgramApiClient(natConfig, mockClient)
    val results = apiClient.searchPrograms("test")

    assertEquals(2, results.size)
    assertTrue(results.containsAll(sampleProgramsPage1 + sampleProgramsPage2))
  }

  @Test
  fun `searchPrograms returns empty list when no programs found`() {
    val emptyResponse =
        buildPagedResponse(
            programs = emptyList(), count = 0, totalCount = 0, offset = 0, nextOffset = null)

    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/programs/search?q=").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(emptyResponse.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = NatProgramApiClient(natConfig, mockClient)
    val results = apiClient.getPrograms()

    assertTrue(results.isEmpty())
  }

  @Test
  fun `searchPrograms throws IOException on unsuccessful response`() {
    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/programs/search?q=fail").build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal Server Error")
            .body("".toResponseBody(null))
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = NatProgramApiClient(natConfig, mockClient)

    assertThrows(IOException::class.java) { apiClient.searchPrograms("fail") }
  }

  @Test
  fun `searchPrograms throws IOException on empty response body`() {
    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/programs/search?q=empty").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(null) // no body
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = NatProgramApiClient(natConfig, mockClient)

    assertThrows(IOException::class.java) { apiClient.searchPrograms("empty") }
  }

  @Test
  fun `getPrograms calls searchPrograms with empty query`() {
    val spyApiClient = spy(NatProgramApiClient(natConfig, mockClient))

    doReturn(emptyList<NatProgram>()).whenever(spyApiClient).searchPrograms("")

    val results = spyApiClient.getPrograms()

    verify(spyApiClient).searchPrograms("")
    assertTrue(results.isEmpty())
  }

  @Test
  @Tag("remoteApi")
  fun `remote endpoint is reachable and returns paged Nat programs`() {
    val realClient = OkHttpClient()
    val realConfig = ApiClientProperties.Nat() // use default baseUrl from config

    val apiClient = NatProgramApiClient(realConfig, realClient)

    val programs = apiClient.getPrograms()

    assertNotNull(programs, "Programs list should not be null")
    assertTrue(programs.isNotEmpty(), "Programs list should not be empty")
  }

  @Test
  @Tag("remoteApi")
  fun `remote endpoint is reachable and returns programs matching search query`() {
    val realClient = OkHttpClient()
    val realConfig = ApiClientProperties.Nat()

    val apiClient = NatProgramApiClient(realConfig, realClient)

    val searchQuery = "M.Sc. Informatik"
    val results = apiClient.searchPrograms(searchQuery)

    assertNotNull(results, "Search results should not be null")
    assertTrue(results.isNotEmpty(), "Search results should not be empty")
    assertTrue(
        results.any { it.degreeProgramName.contains("M.Sc. Informatik", ignoreCase = true) },
        "At least one program name should match the search query")
  }
}
