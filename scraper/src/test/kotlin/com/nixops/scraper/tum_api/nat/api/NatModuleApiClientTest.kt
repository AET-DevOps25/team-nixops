package com.nixops.scraper.tum_api.nat.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nixops.scraper.config.ApiClientProperties
import com.nixops.scraper.tum_api.nat.model.Language
import com.nixops.scraper.tum_api.nat.model.NatModule
import java.io.IOException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class NatModuleApiClientTest {

  private val mockClient: OkHttpClient = mock()
  private val mockCall: Call = mock()
  private val objectMapper = jacksonObjectMapper()
  private val natConfig = ApiClientProperties.Nat.of("https://example.com/api")

  private val sampleModulesPage1 =
      listOf(
          NatModule(
              id = 1,
              code = "MOD101",
              title = "Module 1",
              titleEn = "Module One",
              languages = setOf(Language("Deutsch", "German", "DE", "DE")),
              content = "Content 1",
              contentEn = "Content One",
              outcome = "Outcome 1",
              outcomeEn = "Outcome One",
              methods = "Methods 1",
              methodsEn = "Methods One",
              exam = "Exam 1",
              examEn = "Exam One",
              credits = 5.0f,
              courses = emptyMap()))

  private val sampleModulesPage2 =
      listOf(
          NatModule(
              id = 2,
              code = "MOD102",
              title = "Module 2",
              titleEn = "Module Two",
              languages = setOf(Language("Englisch", "English", "EN", "EN")),
              content = "Content 2",
              contentEn = "Content Two",
              outcome = "Outcome 2",
              outcomeEn = "Outcome Two",
              methods = "Methods 2",
              methodsEn = "Methods Two",
              exam = "Exam 2",
              examEn = "Exam Two",
              credits = 6.0f,
              courses = emptyMap()))

  private fun buildPagedResponse(
      modules: List<NatModule>,
      count: Int,
      totalCount: Int,
      offset: Int,
      nextOffset: Int?
  ): String {
    val pagedResponse =
        PagedResponse(
            hits = modules,
            count = count,
            totalCount = totalCount,
            offset = offset,
            nextOffset = nextOffset)
    return objectMapper.writeValueAsString(pagedResponse)
  }

  @Test
  fun `fetchAllNatModules returns all modules from paged API`() {
    // Setup first page response with nextOffset
    val responsePage1 =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/mhb/module?").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(
                buildPagedResponse(
                        modules = sampleModulesPage1,
                        count = sampleModulesPage1.size,
                        totalCount = 2,
                        offset = 0,
                        nextOffset = 1)
                    .toResponseBody("application/json".toMediaTypeOrNull()))
            .build()

    // Setup second page response with no nextOffset (end)
    val responsePage2 =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/mhb/module?offset=1").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(
                buildPagedResponse(
                        modules = sampleModulesPage2,
                        count = sampleModulesPage2.size,
                        totalCount = 2,
                        offset = 1,
                        nextOffset = null)
                    .toResponseBody("application/json".toMediaTypeOrNull()))
            .build()

    // Mock OkHttpClient to return these responses in order
    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(responsePage1).thenReturn(responsePage2)

    val apiClient = NatModuleApiClient(natConfig, mockClient)
    val results = apiClient.fetchAllNatModules()

    assertEquals(2, results.size)
    assertTrue(results.containsAll(sampleModulesPage1 + sampleModulesPage2))
  }

  @Test
  fun `fetchAllNatModules throws IOException on failed response`() {
    val badResponse =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/mhb/module?").build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal Server Error")
            .body("".toResponseBody(null))
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(badResponse)

    val apiClient = NatModuleApiClient(natConfig, mockClient)

    assertThrows(IOException::class.java) { apiClient.fetchAllNatModules() }
  }

  @Test
  fun `fetchNatModuleDetail returns module on successful response`() {
    val module = sampleModulesPage1.first()

    val responseBody =
        objectMapper
            .writeValueAsString(module)
            .toResponseBody("application/json".toMediaTypeOrNull())

    val response =
        Response.Builder()
            .request(
                Request.Builder().url("${natConfig.baseUrl}/mhb/module/${module.code}").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = NatModuleApiClient(natConfig, mockClient)
    val result = apiClient.fetchNatModuleDetail(module.code)

    assertNotNull(result)
    assertEquals(module, result)
  }

  @Test
  fun `fetchNatModuleDetail returns null on 404`() {
    val moduleCode = "NONEXISTENT"

    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/mhb/module/$moduleCode").build())
            .protocol(Protocol.HTTP_1_1)
            .code(404)
            .message("Not Found")
            .body("".toResponseBody(null))
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = NatModuleApiClient(natConfig, mockClient)
    val result = apiClient.fetchNatModuleDetail(moduleCode)

    assertNull(result)
  }

  @Test
  fun `fetchNatModuleDetail throws IOException on unsuccessful response`() {
    val moduleCode = "MODERROR"

    val response =
        Response.Builder()
            .request(Request.Builder().url("${natConfig.baseUrl}/mhb/module/$moduleCode").build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal Server Error")
            .body("".toResponseBody(null))
            .build()

    whenever(mockClient.newCall(any())).thenReturn(mockCall)
    whenever(mockCall.execute()).thenReturn(response)

    val apiClient = NatModuleApiClient(natConfig, mockClient)

    assertThrows(IOException::class.java) { apiClient.fetchNatModuleDetail(moduleCode) }
  }

  @Test
  @Tag("remoteApi")
  fun `remote endpoint is reachable and returns paged Nat modules`() {
    val realClient = OkHttpClient()
    val realConfig = ApiClientProperties.Nat()

    val apiClient = NatModuleApiClient(realConfig, realClient)

    val modules = apiClient.fetchAllNatModules()

    assertNotNull(modules, "Modules list should not be null")
    assertTrue(modules.isNotEmpty(), "Modules list should not be empty")
  }

  @Test
  @Tag("remoteApi")
  fun `remote endpoint is reachable and returns Nat module detail`() {
    val realClient = OkHttpClient()
    val realConfig = ApiClientProperties.Nat()

    val apiClient = NatModuleApiClient(realConfig, realClient)

    val moduleCode = "IN0001"

    val moduleDetail = apiClient.fetchNatModuleDetail(moduleCode)

    assertNotNull(moduleDetail, "Module detail should not be null")
    assertEquals(moduleCode, moduleDetail?.code, "Module code should match requested code")
  }
}
