package com.nixops.scraper.repository

import com.nixops.scraper.model.Curriculum
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository interface CurriculumRepository : JpaRepository<Curriculum, Long>
