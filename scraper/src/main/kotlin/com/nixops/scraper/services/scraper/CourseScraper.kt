package com.nixops.scraper.services.scraper

import com.nixops.scraper.extensions.genericUpsert
import com.nixops.scraper.model.*
import com.nixops.scraper.services.SemesterService
import com.nixops.scraper.tum_api.campus.api.CampusCourseApiClient
import com.nixops.scraper.tum_api.nat.api.NatCourseApiClient
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class CourseScraper(
    private val natCourseApiClient: NatCourseApiClient,
    private val campusCourseApiClient: CampusCourseApiClient,
    private val semesterService: SemesterService
) {
  fun scrapeCourse(id: Int): Course? {
    return transaction {
      val natCourse = natCourseApiClient.getCourseById(id) ?: return@transaction null
      val campusCourseGroups = campusCourseApiClient.getCourseGroups(id)

      logger.debug("Saving course with id: $id")

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

      val course =
          Courses.genericUpsert(Course) {
            it[Courses.id] = natCourse.courseId
            it[Courses.courseName] = natCourse.courseName
            it[Courses.courseNameEn] = natCourse.courseNameEn
            it[Courses.courseNameList] = natCourse.courseNameList
            it[Courses.courseNameListEn] = natCourse.courseNameListEn
            it[Courses.description] = natCourse.description
            it[Courses.descriptionEn] = natCourse.descriptionEn
            it[Courses.teachingMethod] = natCourse.teachingMethod
            it[Courses.teachingMethodEn] = natCourse.teachingMethodEn
            it[Courses.note] = natCourse.note
            it[Courses.noteEn] = natCourse.noteEn
            it[Courses.activityId] = natCourse.activity?.activityId
            it[Courses.activityName] = natCourse.activity?.activityName
            it[Courses.activityNameEn] = natCourse.activity?.activityNameEn
          }

      if (campusCourseGroups != null) {
        for (campusGroup in campusCourseGroups) {
          val existingGroup = Group.findById(campusGroup.id)
          val group =
              if (existingGroup != null) {
                existingGroup.name = campusGroup.name
                existingGroup
              } else {
                Group.new(campusGroup.id) {
                  name = campusGroup.name
                  this.course = course
                }
              }

          for (campusAppointment in campusGroup.appointments) {
            val appointment =
                Appointments.genericUpsert(Appointment) {
                  it[Appointments.id] = campusAppointment.id
                  it[Appointments.seriesBeginDate] = campusAppointment.seriesBeginDate.value
                  it[Appointments.seriesEndDate] = campusAppointment.seriesEndDate.value
                  it[Appointments.beginTime] = campusAppointment.beginTime
                  it[Appointments.endTime] = campusAppointment.endTime
                  it[Appointments.groupId] = group.id
                }

            Weekday.find { AppointmentWeekdays.appointment eq appointment.id }
                .forEach { it.delete() }

            for (weekday in campusAppointment.weekdays) {
              Weekday.new {
                this.appointment = appointment
                this.name = weekday.key.lowercase().trimEnd('.')
              }
            }
          }
        }
      }

      course
    }
  }

  fun scrapeCourses(curriculumVersionId: Int, termId: Int): Set<Course> {
    val courses = campusCourseApiClient.getCourses(curriculumVersionId, termId)

    return courses.mapNotNull { course -> scrapeCourse(course.id) }.toSet()
  }

  fun scrapeCourses(semester: Semester) {
    val curriculumIds = transaction {
      Curriculum.all().map { curriculum -> curriculum.id.value }.sorted()
    }

    curriculumIds.forEach { curriculumId ->
      logger.info("Fetch courses for $curriculumId ${semester.semesterIdTumOnline}")

      val existing = transaction {
        CurriculumCourses.select(CurriculumCourses.course)
            .where(
                (CurriculumCourses.curriculum eq curriculumId) and
                    (CurriculumCourses.semester eq semester.semesterIdTumOnline))
            .withDistinct()
            .toList()
      }

      if (existing.isNotEmpty()) {
        logger.trace("Courses up-to-date")
        return@forEach
      }

      val campusCourses =
          campusCourseApiClient.getCourses(curriculumId, semester.semesterIdTumOnline)

      val courses =
          campusCourses.mapNotNull { course ->
            transaction { Course.findById(course.id) } ?: scrapeCourse(course.id)
          }

      transaction {
        for (course in courses) {
          CurriculumCourses.insertIgnore {
            it[CurriculumCourses.curriculum] = curriculumId
            it[CurriculumCourses.semester] = semester.semesterIdTumOnline
            it[CurriculumCourses.course] = course.id.value
          }
        }
      }
    }
  }

  fun scrapeCourses(semesterKey: String) {
    val semester = semesterService.getSemester(semesterKey) ?: return
    scrapeCourses(semester)
  }

  fun scrapeCourses() {
    scrapeCourses("lecture")
  }
}
