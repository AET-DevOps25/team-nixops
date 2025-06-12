package com.nixops.scraper.tum_api.nat.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nixops.scraper.tum_api.nat.model.NatModule
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class NatModuleApiClient(
    private val baseUrl: String = "https://api.srv.nat.tum.de/api/v1",
    private val client: OkHttpClient = OkHttpClient()
) {
    private val mapper = jacksonObjectMapper()

    /**
     * Fetch all NatModules (overview) with optional org_id.
     */
    fun fetchAllNatModules(orgId: Int? = null): List<NatModule> {
        val natModules = mutableListOf<NatModule>()
        var nextOffset: Int? = null

        do {
            val urlBuilder = StringBuilder("$baseUrl/mhb/module?")

            orgId?.let { urlBuilder.append("org_id=$it&") }
            nextOffset?.let { urlBuilder.append("offset=$it&") }

            val request = Request.Builder()
                .url(urlBuilder.toString().removeSuffix("&"))
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val page: PagedResponse<NatModule> = mapper.readValue(response.body!!.string())
            natModules.addAll(page.hits)

            println("Fetched ${natModules.size}/${page.totalCount} modules")

            nextOffset = page.nextOffset
        } while (nextOffset != null)

        return natModules
    }

    /**
     * Fetch detailed NatModule info by NatModule_code.
     */
    fun fetchNatModuleDetail(NatModuleCode: String): NatModule {
        val request = Request.Builder()
            .url("$baseUrl/mhb/module/$NatModuleCode")
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        return mapper.readValue(response.body!!.string())
    }

    /**
     * Combined function: Fetch overview and then full details per NatModule.
     */
    fun fetchAllNatModulesWithDetails(orgId: Int): List<NatModule> {
        val overviewNatModules = fetchAllNatModules(orgId)
        println("Get Module Details")

        return overviewNatModules.mapNotNullIndexed { index, natModule ->
            natModule.moduleCode?.let {
                println("Fetching detail for module ${index + 1} of ${overviewNatModules.size}: $it")
                fetchNatModuleDetail(it)
            }
        }
    }
}

inline fun <T, R : Any> Iterable<T>.mapNotNullIndexed(transform: (index: Int, T) -> R?): List<R> {
    val destination = ArrayList<R>()
    var index = 0
    for (item in this) {
        val result = transform(index++, item)
        if (result != null) {
            destination.add(result)
        }
    }
    return destination
}