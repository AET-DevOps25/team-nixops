package com.nixops.scraper.config

import com.nixops.scraper.model.*
import javax.sql.DataSource
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger {}

@Configuration
class DatabaseConfig {

  @Bean
  fun exposedDatabase(dataSource: DataSource): Database {
    val db = Database.connect(dataSource)
    transaction(db) {
      SchemaUtils.create(LastUpdated)
      SchemaUtils.create(Semesters)
      SchemaUtils.create(Curriculums)
      SchemaUtils.create(Modules)
      SchemaUtils.create(StudyPrograms)
      SchemaUtils.create(Courses)
      SchemaUtils.create(ModuleCourses)
      SchemaUtils.create(CurriculumCourses)
      SchemaUtils.create(Groups)
      SchemaUtils.create(Appointments)
      SchemaUtils.create(AppointmentWeekdays)
      SchemaUtils.create(StudyProgramSemester)
    }
    logger.info("Exposed connected and tables created")
    return db
  }
}
