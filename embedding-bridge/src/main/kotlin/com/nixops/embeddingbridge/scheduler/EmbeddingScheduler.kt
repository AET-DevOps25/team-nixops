package com.nixops.embeddingbridge.scheduler

import com.nixops.embeddingbridge.services.EmbeddingBridgeService
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
