package com.nixops.scraper.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "scraper.embedding")
class EmbeddingProperties {
  var endpoint: String = "http://localhost:8000/embed"
}
