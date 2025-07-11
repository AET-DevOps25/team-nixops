package com.nixops.embedderbridge.config

import com.nixops.openapi.scraper.api.DefaultApi
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ScraperApiClientConfig(private val okHttpClient: OkHttpClient) {
  @Value("\${embedder-bridge.scraper.base-url}") lateinit var baseUrl: String

  @Bean
  fun scraperClient(): DefaultApi {
    return DefaultApi(basePath = baseUrl, client = okHttpClient)
  }
}
