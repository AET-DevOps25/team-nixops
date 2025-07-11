package com.nixops.embeddingbridge.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component

@Component
class EmbeddingMetrics(private val meterRegistry: MeterRegistry) {
  fun recordEmbedding(studyId: Long, programName: String, degreeType: String, block: () -> Unit) {
    Timer.builder("embedding_bridge.study_programs.embedding_duration")
        .description("Duration of scrape")
        .tag("studyId", studyId.toString())
        .tag("programName", programName)
        .tag("degreeType", degreeType)
        .register(meterRegistry)
        .record(block)

    val counterName = "embedding_bridge.study_programs.embedded"
    meterRegistry
        .counter(
            counterName,
            "studyId",
            studyId.toString(),
            "programName",
            programName,
            "degreeType",
            degreeType)
        .increment()
  }
}
