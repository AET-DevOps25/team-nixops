package com.nixops.scraper.services

import Course
import com.nixops.scraper.model.CurriculumCourses
import com.nixops.scraper.model.Semester
import com.nixops.scraper.model.StudyProgram
import com.nixops.scraper.services.scraper.CourseScraper
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CourseService(
    private val curriculumService: CurriculumService,
    private val courseScraper: CourseScraper,
) {
  fun getCourse(id: Int): Course? {
    return transaction { Course.findById(id) } ?: courseScraper.scrapeCourse(id)
  }

  fun getCourses(studyProgram: StudyProgram, semester: Semester): Set<Course> {
    val curriculum = curriculumService.getCurriculum(studyProgram, semester) ?: return setOf()

    val existing = transaction {
      CurriculumCourses.select(CurriculumCourses.course)
          .where(
              (CurriculumCourses.curriculum eq curriculum.id.value) and
                  (CurriculumCourses.semester eq semester.semesterIdTumOnline))
          .withDistinct()
          .mapNotNull { getCourse(it[CurriculumCourses.course]) }
    }

    if (existing.isNotEmpty()) {
      return existing.toSet()
    }

    val courses = courseScraper.scrapeCourses(curriculum.id.value, semester.semesterIdTumOnline)

    for (course in courses) {
      CurriculumCourses.insertIgnore {
        it[CurriculumCourses.curriculum] = curriculum.id.value
        it[CurriculumCourses.semester] = semester.semesterIdTumOnline
        it[CurriculumCourses.course] = course.id.value
      }
    }

    return courses
  }
}
