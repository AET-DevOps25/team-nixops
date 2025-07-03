package com.nixops.scraper.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "scraper.api-client")
class ApiClientProperties {
  lateinit var nat: Nat
  lateinit var campus: Campus

  class Nat {
    var baseUrl: String = "https://api.srv.nat.tum.de/api/v1"

    companion object {
      fun of(baseUrl: String) = Nat().apply { this.baseUrl = baseUrl }
    }
  }

  class Campus {
    var baseUrl: String = "https://campus.tum.de/tumonline/ee/rest/"

    companion object {
      fun of(baseUrl: String) = Campus().apply { this.baseUrl = baseUrl }
    }
  }
}
