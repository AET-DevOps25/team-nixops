package com.nixops.scraper.model

import java.time.Duration
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object LastUpdated : Table("last_updated") {
  val key = varchar("key", 100).uniqueIndex()
  val timestamp = datetime("timestamp")
}

fun setLastUpdated(key: String, time: LocalDateTime = LocalDateTime.now()) {
  transaction {
    LastUpdated.insertIgnore() {
      it[LastUpdated.key] = key
      it[LastUpdated.timestamp] = time
    }
    LastUpdated.update({ LastUpdated.key eq key }) { it[LastUpdated.timestamp] = time }
  }
}

fun getLastUpdated(key: String): LocalDateTime? {
  return transaction {
    LastUpdated.select(listOf(LastUpdated.key eq key)).firstOrNull()?.get(LastUpdated.timestamp)
  }
}

fun getTimeSinceLastUpdated(key: String): Duration? {
  val lastUpdatedTime = getLastUpdated(key) ?: return null
  val now = LocalDateTime.now()
  return Duration.between(lastUpdatedTime, now)
}
