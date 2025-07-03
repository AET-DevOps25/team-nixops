package com.nixops.scraper.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@Configuration
class SchedulerConfig {
  @Bean
  fun taskScheduler(): ThreadPoolTaskScheduler {
    val scheduler = ThreadPoolTaskScheduler()
    scheduler.poolSize = 10
    scheduler.setThreadNamePrefix("thread-")
    scheduler.initialize()
    return scheduler
  }
}
