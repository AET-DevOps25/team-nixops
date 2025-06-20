package com.nixops.scraper.repository

import com.nixops.scraper.model.StudyProgram
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository interface StudyProgramRepository : JpaRepository<StudyProgram, Long>
