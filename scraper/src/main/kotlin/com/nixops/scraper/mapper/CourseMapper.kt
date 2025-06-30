package com.nixops.scraper.mapper

import com.nixops.openapi.model.Appointment
import com.nixops.scraper.model.Course
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
class CourseMapper {
  fun courseToApiCourse(course: Course): com.nixops.openapi.model.Course {
    val appointments = mutableListOf<Appointment>()

    course.groups.forEach { group ->
      run {
        val groupAppointments =
            group.appointments.map { appointment ->
              Appointment(
                  appointmentId = appointment.id.value,
                  seriesBeginDate = LocalDateTime.parse(appointment.seriesBeginDate).toLocalDate(),
                  seriesEndDate = LocalDateTime.parse(appointment.seriesEndDate).toLocalDate(),
                  beginTime = appointment.beginTime,
                  endTime = appointment.endTime,
                  weekdays =
                      appointment.weekdays.map { weekday ->
                        Appointment.Weekdays.forValue(weekday.name)
                      })
            }

        appointments.addAll(groupAppointments)
      }
    }

    return com.nixops.openapi.model.Course(
        courseId = course.id.value,
        courseType = course.activityName,
        courseName = course.courseName,
        courseNameEn = course.courseNameEn,
        courseNameList = course.courseNameList,
        courseNameListEn = course.courseNameListEn,
        appointments = appointments)
  }
}
