package com.nixops.scraper.config

import Courses
import Degrees
import com.nixops.scraper.model.*
import javax.sql.DataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DatabaseConfig {

  @Bean
  fun exposedDatabase(dataSource: DataSource): Database {
    val db = Database.connect(dataSource)
    transaction(db) {
      SchemaUtils.create(Users)
      SchemaUtils.create(LastUpdated)
      SchemaUtils.create(Semesters)
      SchemaUtils.create(Curriculums)
      SchemaUtils.create(Modules)
      SchemaUtils.create(StudyPrograms)
      SchemaUtils.create(Courses)
      SchemaUtils.create(Degrees)
    }
    println(">>> Exposed connected and tables created!")
    return db
  }
}
