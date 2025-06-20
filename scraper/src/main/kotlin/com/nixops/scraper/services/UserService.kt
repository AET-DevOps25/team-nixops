package com.nixops.scraper.services

import com.nixops.scraper.model.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class UserService(private val db: Database) {

  fun createUser(name: String): Int {
    return transaction { Users.insert { it[Users.name] = name } get Users.id }
  }

  fun getAllUsers(): List<String> {
    return transaction { Users.selectAll().map { it[Users.name] } }
  }
}
