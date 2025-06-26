package com.nixops.scraper.services.scraper

import Course
import com.nixops.scraper.tum_api.campus.api.CampusCourseApiClient
import com.nixops.scraper.tum_api.nat.api.NatCourseApiClient
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CourseScraper(
    private val natCourseApiClient: NatCourseApiClient,
    private val campusCourseApiClient: CampusCourseApiClient
) {
  fun scrapeCourse(id: Int): Course? {
    return transaction {
      val natCourse = natCourseApiClient.getCourseById(id) ?: return@transaction null
      println("Saving course with id: $id")

      /* natCourse.modules.let {
          for ((semester, courses) in natModule.courses.entries) {
              for (course in courses) {
                  ModuleCourses.insertIgnore {
                      it[ModuleCourses.semester] = semester
                      it[ModuleCourses.module] = natModule.id
                      it[ModuleCourses.course] = course.courseId
                  }
              }
          }
      } */

      val existing = Course.findById(natCourse.courseId)
      if (existing != null) {
        existing.courseName = natCourse.courseName
        existing.courseNameEn = natCourse.courseNameEn
        existing.courseNameList = natCourse.courseNameList
        existing.courseNameListEn = natCourse.courseNameListEn
        existing.description = natCourse.description
        existing.descriptionEn = natCourse.descriptionEn
        existing.teachingMethod = natCourse.teachingMethod
        existing.teachingMethodEn = natCourse.teachingMethodEn
        existing.note = natCourse.note
        existing.noteEn = natCourse.noteEn
        existing.activityId = natCourse.activity?.activityId
        existing.activityName = natCourse.activity?.activityName
        existing.activityNameEn = natCourse.activity?.activityNameEn
        existing
      } else {
        Course.new(natCourse.courseId) {
          courseName = natCourse.courseName
          courseNameEn = natCourse.courseNameEn
          courseNameList = natCourse.courseNameList
          courseNameListEn = natCourse.courseNameListEn
          description = natCourse.description
          descriptionEn = natCourse.descriptionEn
          teachingMethod = natCourse.teachingMethod
          teachingMethodEn = natCourse.teachingMethodEn
          note = natCourse.note
          noteEn = natCourse.noteEn
          activityId = natCourse.activity?.activityId
          activityName = natCourse.activity?.activityName
          activityNameEn = natCourse.activity?.activityNameEn
        }
      }
    }
  }

  fun scrapeCourses(curriculumVersionId: Int, termId: Int): Set<Course> {
    val courses = campusCourseApiClient.getCourses(curriculumVersionId, termId)

    return courses.mapNotNull { course -> scrapeCourse(course.id) }.toSet()
  }
}
