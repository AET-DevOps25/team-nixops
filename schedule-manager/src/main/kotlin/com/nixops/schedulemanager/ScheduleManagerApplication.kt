package com.nixops.schedulemanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

data class EmbeddingCandidate(
    val id: Long,
    val name: String,
    val semester: String,
)

@SpringBootApplication @EnableScheduling class ScheduleManagerApplication()

fun main(args: Array<String>) {
  runApplication<ScheduleManagerApplication>(*args)
}
