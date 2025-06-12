package com.nixops.scraper.repository

import com.nixops.scraper.model.Degree
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DegreeRepository : JpaRepository<Degree, Long>
