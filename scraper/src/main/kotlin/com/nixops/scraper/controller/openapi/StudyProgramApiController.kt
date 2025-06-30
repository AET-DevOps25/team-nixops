package com.nixops.scraper.controller.openapi

import com.nixops.openapi.api.StudyProgramsApi
import com.nixops.openapi.model.*
import com.nixops.scraper.services.CourseService
import com.nixops.scraper.services.ModuleService
import com.nixops.scraper.services.SemesterService
import com.nixops.scraper.services.StudyProgramService
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class StudyProgramsApiController(
    private val studyProgramService: StudyProgramService,
    private val semesterService: SemesterService,
    private val moduleService: ModuleService,
    private val courseService: CourseService,
) : StudyProgramsApi {
  override fun getStudyPrograms(): ResponseEntity<List<StudyProgram>> {
    val studyPrograms =
        studyProgramService.getStudyPrograms().map { studyProgram ->
          StudyProgram(
              studyId = studyProgram.studyId,
              programName = studyProgram.programName,
              degreeProgramName = studyProgram.degreeProgramName,
              degreeTypeName = studyProgram.degreeTypeName,
          )
        }
    return ResponseEntity.ok(studyPrograms)
  }

  override fun getStudyProgram(studyId: Long, semesterKey: String): ResponseEntity<StudyProgram> {
    val studyProgram =
        studyProgramService.getStudyProgram(studyId) ?: return ResponseEntity.notFound().build()

    val semester =
        semesterService.getSemester(semesterKey) ?: return ResponseEntity.notFound().build()

    val modules =
        moduleService.getModules(studyId, semester) ?: return ResponseEntity.notFound().build()

    val apiModules = transaction {
      modules.map { module ->
        val lectures = mutableListOf<Course>()
        val tutorials = mutableListOf<Course>()
        val other = mutableListOf<Course>()

        val courses = courseService.getCourses(module, semester)

        for (course in courses) {
          when (course.activityName) {
            "Vorlesung",
            "Vorlesung mit integrierten Übungen" -> {
              val group = course.groups.find { group -> group.name == "Standardgruppe" }

              val appointments =
                  group?.appointments?.map { appointment ->
                    Appointment(
                        appointmentId = appointment.id.value,
                        seriesBeginDate =
                            LocalDateTime.parse(appointment.seriesBeginDate).toLocalDate(),
                        seriesEndDate =
                            LocalDateTime.parse(appointment.seriesEndDate).toLocalDate(),
                        beginTime = appointment.beginTime,
                        endTime = appointment.endTime,
                        weekdays =
                            appointment.weekdays.map { weekday ->
                              Appointment.Weekdays.forValue(weekday.name)
                            })
                  } ?: listOf()

              val apiCourse =
                  Course(
                      courseId = course.id.value,
                      courseType = course.activityName,
                      courseName = course.courseName,
                      courseNameEn = course.courseNameEn,
                      courseNameList = course.courseNameList,
                      courseNameListEn = course.courseNameListEn,
                      appointments = appointments)

              lectures.add(apiCourse)
            }

            "Tutorium",
            "Übung" -> {
              val appointments = mutableListOf<Appointment>()

              course.groups.forEach { group ->
                run {
                  val groupAppointments =
                      group.appointments.map { appointment ->
                        Appointment(
                            appointmentId = appointment.id.value,
                            seriesBeginDate =
                                LocalDateTime.parse(appointment.seriesBeginDate).toLocalDate(),
                            seriesEndDate =
                                LocalDateTime.parse(appointment.seriesEndDate).toLocalDate(),
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

              val apiCourse =
                  Course(
                      courseId = course.id.value,
                      courseType = course.activityName,
                      courseName = course.courseName,
                      courseNameEn = course.courseNameEn,
                      courseNameList = course.courseNameList,
                      courseNameListEn = course.courseNameListEn,
                      appointments = appointments)

              tutorials.add(apiCourse)
            }

            else -> {
              val appointments = mutableListOf<Appointment>()

              course.groups.forEach { group ->
                run {
                  val groupAppointments =
                      group.appointments.map { appointment ->
                        Appointment(
                            appointmentId = appointment.id.value,
                            seriesBeginDate =
                                LocalDateTime.parse(appointment.seriesBeginDate).toLocalDate(),
                            seriesEndDate =
                                LocalDateTime.parse(appointment.seriesEndDate).toLocalDate(),
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

              val apiCourse =
                  Course(
                      courseId = course.id.value,
                      courseType = course.activityName,
                      courseName = course.courseName,
                      courseNameEn = course.courseNameEn,
                      courseNameList = course.courseNameList,
                      courseNameListEn = course.courseNameListEn,
                      appointments = appointments)

              other.add(apiCourse)
            }
          }
        }

        val moduleCourse = ModuleCourses(lectures = lectures, tutorials = tutorials, other = other)

        Module(
            id = module.id.value.toString(),
            code = module.moduleCode,
            title = module.moduleTitle,
            titleEn = module.moduleTitleEn,
            content = module.moduleContent,
            contentEn = module.moduleContentEn,
            outcome = module.moduleOutcome,
            outcomeEn = module.moduleOutcomeEn,
            methods = module.moduleMethods,
            methodsEn = module.moduleMethodsEn,
            exam = module.moduleExam,
            examEn = module.moduleExamEn,
            credits = module.moduleCredits,
            courses = moduleCourse)
      }
    }

    val apiStudyProgram =
        StudyProgram(
            studyId = studyProgram.studyId,
            programName = studyProgram.programName,
            degreeProgramName = studyProgram.degreeProgramName,
            degreeTypeName = studyProgram.degreeTypeName,
            semesters = mapOf(semester.id.value to apiModules))

    return ResponseEntity.ok(apiStudyProgram)
  }
}
