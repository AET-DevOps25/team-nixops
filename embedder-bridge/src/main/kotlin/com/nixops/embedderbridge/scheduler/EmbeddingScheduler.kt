package com.nixops.embedderbridge.scheduler

import com.nixops.embedderbridge.services.EmbeddingBridgeService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class EmbeddingScheduler(
    private val embeddingBridgeService: EmbeddingBridgeService,
    private val fixedRateString: String
) {
  @Scheduled(fixedRateString = "#{@fixedRateString}")
  fun embed() {
    embeddingBridgeService.embedNextCandidate()
  }
}
