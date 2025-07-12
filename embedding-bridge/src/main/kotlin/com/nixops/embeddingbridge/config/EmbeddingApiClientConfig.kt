package com.nixops.embeddingbridge.config

import com.nixops.openapi.genai.api.EmbeddingApi
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EmbeddingApiClientConfig(private val okHttpClient: OkHttpClient) {
  @Value("\${embedding-bridge.embedding.base-url}") lateinit var baseUrl: String

  @Bean
  fun embeddingClient(): EmbeddingApi {
    return EmbeddingApi(basePath = baseUrl, client = okHttpClient)
  }
}
