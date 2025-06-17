package com.nixops.scraper.config

import com.nixops.scraper.model.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

@Configuration
class DatabaseConfig {

    @Bean
    fun exposedDatabase(): Database {
        val dataSource = DriverManagerDataSource().apply {
            setDriverClassName("org.postgresql.Driver")
            url = "jdbc:postgresql://localhost:5433/your_db"
            username = "your_user"
            password = "your_password"
        }
        val db = Database.connect(dataSource)
        transaction(db) {
            SchemaUtils.create(Users) // this creates the table if missing
        }
        println(">>> Exposed connected and tables created!")
        return db
    }
}
