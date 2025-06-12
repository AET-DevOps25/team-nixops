package com.nixops.scraper.mapper

import com.nixops.scraper.model.Course
import com.nixops.scraper.model.Module
import com.nixops.scraper.model.SemesterCourses
import com.nixops.scraper.tum_api.nat.model.NatCourse
import org.mapstruct.Named
import org.springframework.stereotype.Component

@Component
class SemesterCoursesMappingHelper(
    private val courseMapper: CourseMapper
) {

    @Named("mapSemesterCourses")
    fun mapSemesterCourses(
        input: Map<String, List<NatCourse>>?,
    ): List<SemesterCourses> {
        if (input == null) return emptyList()

        return input.map { (semester, natCourses) ->
            val courses = natCourses.map { courseMapper.natCourseToCourse(it) }.toMutableSet()

            SemesterCourses(
                semester = semester,
                courses = courses
            )
        }
    }
}
