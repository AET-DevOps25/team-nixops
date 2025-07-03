package com.nixops.scraper.config

import com.nixops.scraper.tum_api.campus.api.CampusCourseApiClient
import com.nixops.scraper.tum_api.campus.api.CampusCurriculumApiClient
import com.nixops.scraper.tum_api.nat.api.NatCourseApiClient
import com.nixops.scraper.tum_api.nat.api.NatModuleApiClient
import com.nixops.scraper.tum_api.nat.api.NatProgramApiClient
import com.nixops.scraper.tum_api.nat.api.NatSemesterApiClient
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApiClientConfig(private val apiClientProperties: ApiClientProperties) {
  @Bean fun natApiClientProperties() = apiClientProperties.nat

  @Bean fun campusApiClientProperties() = apiClientProperties.campus

  @Bean
  fun okHttpClient(): OkHttpClient {
    val cacheSize = Long.MAX_VALUE
    val cacheDirectory = File("cache_directory")
    val cache = Cache(cacheDirectory, cacheSize)

    val cacheControl = CacheControl.Builder().maxAge(5, TimeUnit.DAYS).build()

    return OkHttpClient.Builder()
        .cache(cache)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addNetworkInterceptor { chain ->
          val response = chain.proceed(chain.request())
          if (response.header("Cache-Control") == null) {
            response
                .newBuilder()
                .removeHeader("Pragma")
                .header("Cache-Control", cacheControl.toString())
                .build()
          } else {
            response
          }
        }
        .build()
  }

  @Bean
  fun campusCurriculumApiClient(okHttpClient: OkHttpClient) =
      CampusCurriculumApiClient(
          client = okHttpClient, campusApiClientProperties = campusApiClientProperties())

  @Bean
  fun campusCourseApiClient(okHttpClient: OkHttpClient) =
      CampusCourseApiClient(
          client = okHttpClient, campusApiClientProperties = campusApiClientProperties())

  @Bean
  fun natSemesterApiClient(okHttpClient: OkHttpClient) =
      NatSemesterApiClient(client = okHttpClient, natApiClientProperties = natApiClientProperties())

  @Bean
  fun natProgramApiClient(okHttpClient: OkHttpClient) =
      NatProgramApiClient(client = okHttpClient, natApiClientProperties = natApiClientProperties())

  @Bean
  fun natModuleApiClient(okHttpClient: OkHttpClient) =
      NatModuleApiClient(client = okHttpClient, natApiClientProperties = natApiClientProperties())

  @Bean
  fun natCourseApiClient(okHttpClient: OkHttpClient) =
      NatCourseApiClient(client = okHttpClient, natApiClientProperties = natApiClientProperties())
}
