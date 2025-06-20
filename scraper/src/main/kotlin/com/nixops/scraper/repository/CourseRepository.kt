package com.nixops.scraper.repository

import com.nixops.scraper.model.Course
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository interface CourseRepository : JpaRepository<Course, Long>
