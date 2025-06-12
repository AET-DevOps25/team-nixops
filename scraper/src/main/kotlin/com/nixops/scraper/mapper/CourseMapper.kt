package com.nixops.scraper.mapper

import com.nixops.scraper.model.Course
import com.nixops.scraper.tum_api.nat.model.NatCourse
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface CourseMapper {
    @Mapping(source = "courseId", target = "id")
    @Mapping(target = "semesterCourses", ignore = true)
    fun natCourseToCourse(natCourse: NatCourse): Course
}