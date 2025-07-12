package com.nixops.embeddingbridge.config

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OkHttpClientConfig {

  @Bean
  fun okHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(1, TimeUnit.HOURS)
        .build()
  }
}
