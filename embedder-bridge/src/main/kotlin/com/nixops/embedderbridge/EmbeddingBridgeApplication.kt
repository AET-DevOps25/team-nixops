package com.nixops.embedderbridge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

data class EmbeddingCandidate(
    val id: Long,
    val name: String,
    val semester: String,
)

@SpringBootApplication @EnableScheduling class EmbeddingBridgeApplication()

fun main(args: Array<String>) {
  runApplication<EmbeddingBridgeApplication>(*args)
}
