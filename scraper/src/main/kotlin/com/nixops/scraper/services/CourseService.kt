package com.nixops.scraper.services

import com.nixops.scraper.model.Semester
import com.nixops.scraper.model.StudyProgram
import com.nixops.scraper.tum_api.campus.api.CampusCourseApiClient
import org.springframework.stereotype.Service

@Service
class CourseService(
    private val curriculumService: CurriculumService,
    private val campusCourseApiClient: CampusCourseApiClient
) {
  fun getCourseIds(studyProgram: StudyProgram, semester: Semester): Set<Int> {
    val curriculum = curriculumService.getCurriculum(studyProgram, semester) ?: return setOf()

    val courses =
        campusCourseApiClient.getCourses(curriculum.id.value, semester.semesterIdTumOnline)
    return courses.map { course -> course.id }.toSet()
  }
}
