package com.nixops.schedulingEngine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class SchedulingApplication

fun main(args: Array<String>) {
  runApplication<SchedulingApplication>(*args)
}
