package com.nixops.scraper.config

import com.nixops.scraper.model.LastUpdated
import com.nixops.scraper.model.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DriverManagerDataSource

@Configuration
class DatabaseConfig {

  @Bean
  fun exposedDatabase(): Database {
    val dataSource =
        DriverManagerDataSource().apply {
          setDriverClassName("org.postgresql.Driver")
          url = "jdbc:postgresql://localhost:5433/your_db"
          username = "your_user"
          password = "your_password"
        }
    val db = Database.connect(dataSource)
    transaction(db) {
      SchemaUtils.create(Users)
      SchemaUtils.create(LastUpdated)
    }
    println(">>> Exposed connected and tables created!")
    return db
  }
}
