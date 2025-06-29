package com.nixops.scraper.config

import Courses
import com.nixops.scraper.model.*
import javax.sql.DataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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

      ensureLastModifiedFunctionExists()
      addLastModifiedTrigger(Courses)
    }
    println(">>> Exposed connected and tables created!")
    return db
  }

  fun ensureLastModifiedFunctionExists() {
    transaction {
      exec(
          """
            CREATE OR REPLACE FUNCTION update_last_modified_column()
            RETURNS TRIGGER AS $$
            BEGIN
              IF row_to_json(NEW) IS DISTINCT FROM row_to_json(OLD) THEN
                NEW.last_modified = now();
              END IF;
              RETURN NEW;
            END;
            $$ LANGUAGE plpgsql;
        """
              .trimIndent())
    }
  }

  fun addLastModifiedTrigger(table: Table) {
    val tableName = table.tableName
    transaction {
      exec(
          """
        DO $$
        BEGIN
          IF NOT EXISTS (
            SELECT 1 FROM pg_trigger WHERE tgname = 'set_last_modified_$tableName'
          ) THEN
            CREATE TRIGGER set_last_modified_$tableName
            BEFORE UPDATE ON $tableName
            FOR EACH ROW
            EXECUTE FUNCTION update_last_modified_column();
          END IF;
        END;
        $$;
    """
              .trimIndent())
    }
  }
}
