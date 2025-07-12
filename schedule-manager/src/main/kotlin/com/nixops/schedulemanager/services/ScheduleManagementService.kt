package com.nixops.schedulemanager.services

import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ScheduleManagementService {
  private val scheduleCache =
      Caffeine.newBuilder()
          .expireAfterAccess(1, TimeUnit.MINUTES)
          .build<String, CopyOnWriteArrayList<String>>()

  fun addModule(scheduleId: String, module: String) {
    scheduleCache.asMap().compute(scheduleId) { _, list ->
      (list ?: CopyOnWriteArrayList()).apply { add(module) }
    }
  }

  fun getSchedule(scheduleId: String): List<String>? {
    return scheduleCache.getIfPresent(scheduleId)
  }
}
