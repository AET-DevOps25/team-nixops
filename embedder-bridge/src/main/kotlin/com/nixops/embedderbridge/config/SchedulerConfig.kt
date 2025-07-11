package com.nixops.embedderbridge.config

import java.time.Duration
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SchedulerConfig(@Value("\${embedding-bridge.interval}") private val fixedRate: Duration) {
  @Bean fun fixedRateString(): String = fixedRate.toMillis().toString()
}
