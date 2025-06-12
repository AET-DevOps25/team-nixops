package com.nixops.scraper.mapper

import com.nixops.scraper.model.Module
import com.nixops.scraper.tum_api.nat.model.NatModule
import org.mapstruct.Mapper
import org.mapstruct.Mapping


@Mapper(componentModel = "spring", uses = [SemesterCoursesMappingHelper::class, CourseMapper::class])
interface ModuleMapper {

    @Mapping(source = "courses", target = "semesterCourses", qualifiedByName = ["mapSemesterCourses"])
    fun natModuleToModule(natModule: NatModule): Module
}