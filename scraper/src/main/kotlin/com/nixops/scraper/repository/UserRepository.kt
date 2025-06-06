package com.nixops.scraper.repository

import com.nixops.scraper.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>
