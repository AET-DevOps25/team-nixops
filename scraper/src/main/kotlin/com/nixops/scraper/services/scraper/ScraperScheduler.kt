package com.nixops.scraper.services.scraper

import com.nixops.scraper.metrics.ScraperMetrics
import com.nixops.scraper.model.*
import java.time.Duration
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ScraperScheduler(
    private val semesterScraper: SemesterScraper,
    private val curriculumScraper: CurriculumScraper,
    private val moduleScraper: ModuleScraper,
    private val studyProgramScraper: StudyProgramScraper,
    private val courseScraper: CourseScraper,
    //
    private val scraperMetrics: ScraperMetrics
) {
  fun check(name: String, scrape: () -> Unit, interval: Duration = Duration.ofHours(2)) {
    val lastUpdated = getTimeSinceLastUpdated(name)
    if (lastUpdated == null || lastUpdated > interval) {
      logger.info("Scraping $name")

      scraperMetrics.recordScrapeDuration(name) { scrape() }

      setLastUpdated(name)
    }
  }

  @Scheduled(fixedRate = 2 * 1000)
  fun check() {
    check("semesters", semesterScraper::scrapeSemesters)
    check("study_programs", studyProgramScraper::scrapeStudyPrograms)
    check("curricula", curriculumScraper::scrapeCurricula)
    check("modules", moduleScraper::scrapeModules, Duration.ofDays(2))
    check("courses", courseScraper::scrapeCourses, Duration.ofDays(2))
  }
}
