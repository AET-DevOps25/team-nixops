package com.nixops.scraper.tum_api.nat.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nixops.scraper.config.ApiClientProperties
import com.nixops.scraper.tum_api.nat.model.NatModule
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request

class NatModuleApiClient(
    natApiClientProperties: ApiClientProperties.Nat,
    private val client: OkHttpClient = OkHttpClient()
) {
  private val mapper = jacksonObjectMapper()
  private val baseUrl: String = natApiClientProperties.baseUrl

  /** Fetch all NatModules (overview) with optional org_id. */
  @Throws(IOException::class)
  fun fetchAllNatModules(orgId: Int? = null): List<NatModule> {
    val natModules = mutableListOf<NatModule>()
    var nextOffset: Int? = null

    do {
      val urlBuilder = StringBuilder("$baseUrl/mhb/module?")

      orgId?.let { urlBuilder.append("org_id=$it&") }
      nextOffset?.let { urlBuilder.append("offset=$it&") }

      val request = Request.Builder().url(urlBuilder.toString().removeSuffix("&")).build()

      val response = client.newCall(request).execute()

      if (!response.isSuccessful) throw IOException("Unexpected code $response")

      val page: PagedResponse<NatModule> = mapper.readValue(response.body!!.string())
      natModules.addAll(page.hits)

      println("Fetched ${natModules.size}/${page.totalCount} modules")

      nextOffset = page.nextOffset
    } while (nextOffset != null)

    return natModules
  }

  /** Fetch detailed NatModule info by NatModule_code. */
  @Throws(IOException::class)
  fun fetchNatModuleDetail(moduleCode: String): NatModule? {
    val request = Request.Builder().url("$baseUrl/mhb/module/$moduleCode").build()

    val response = client.newCall(request).execute()

    if (response.code == 404) return null

    if (!response.isSuccessful) throw IOException("Unexpected code $response")

    return mapper.readValue(response.body!!.string())
  }

  /** Combined function: Fetch overview and then full details per NatModule. */
  @Throws(IOException::class)
  fun fetchAllNatModulesWithDetails(orgId: Int): List<NatModule> {
    val overviewNatModules = fetchAllNatModules(orgId)
    println("Get Module Details")

    return overviewNatModules.mapNotNullIndexed { index, natModule ->
      natModule.code.let {
        println("Fetching detail for module ${index + 1} of ${overviewNatModules.size}: $it")
        fetchNatModuleDetail(it)
      }
    }
  }

  private inline fun <T, R : Any> Iterable<T>.mapNotNullIndexed(
      transform: (index: Int, T) -> R?
  ): List<R> {
    val destination = ArrayList<R>()
    for ((index, item) in this.withIndex()) {
      val result = transform(index, item)
      if (result != null) {
        destination.add(result)
      }
    }
    return destination
  }
}
