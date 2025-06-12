package com.nixops.scraper.repository


import com.nixops.scraper.model.Module
import com.nixops.scraper.model.Semester
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SemesterRepository : JpaRepository<Semester, Long> {
    fun findBySemesterKey(semesterKey: String): Semester?
}