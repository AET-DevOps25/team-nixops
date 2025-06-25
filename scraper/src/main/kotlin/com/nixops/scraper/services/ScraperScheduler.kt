package com.nixops.scraper.services

import com.nixops.scraper.model.*
import com.nixops.scraper.services.scraper.CurriculumScraper
import com.nixops.scraper.services.scraper.ModuleScraper
import com.nixops.scraper.services.scraper.SemesterScraper
import com.nixops.scraper.services.scraper.StudyProgramScraper
import java.time.Duration
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScraperScheduler(
    private val semesterScraper: SemesterScraper,
    private val curriculumScraper: CurriculumScraper,
    private val moduleScraper: ModuleScraper,
    private val studyProgramScraper: StudyProgramScraper,
) {
  fun check(name: String, scrape: () -> Unit) {
    val lastUpdated = getTimeSinceLastUpdated(name)
    if (lastUpdated == null || lastUpdated > Duration.ofHours(2)) {
      println("should update $name")
      scrape()
      setLastUpdated(name)
    }
  }

  @Scheduled(fixedRate = 2 * 1000)
  fun check() {
    check("semesters", semesterScraper::scrapeSemesters)
    check("study_programs", studyProgramScraper::scrapeStudyPrograms)
    check("curricula", curriculumScraper::scrapeCurricula)
    check("modules", moduleScraper::scrapeModules)
    // check("courses", ::scrapeCourses)
  }
}
