package com.nixops.embedderbridge.mapper

import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface AppointmentMapper {
  fun map(
      src: com.nixops.openapi.scraper.model.Appointment
  ): com.nixops.openapi.genai.model.Appointment
}

@Mapper(componentModel = "spring", uses = [AppointmentMapper::class])
interface CourseMapper {
  fun map(src: com.nixops.openapi.scraper.model.Course): com.nixops.openapi.genai.model.Course
}

@Mapper(componentModel = "spring", uses = [CourseMapper::class])
interface ModuleCoursesMapper {
  fun map(
      src: com.nixops.openapi.scraper.model.ModuleCourses
  ): com.nixops.openapi.genai.model.ModuleCourses
}

@Mapper(componentModel = "spring", uses = [ModuleCoursesMapper::class])
interface ModuleMapper {
  fun map(src: com.nixops.openapi.scraper.model.Module): com.nixops.openapi.genai.model.Module

  fun mapList(
      src: List<com.nixops.openapi.scraper.model.Module>
  ): List<com.nixops.openapi.genai.model.Module>

  fun mapMap(
      src: Map<String, List<com.nixops.openapi.scraper.model.Module>>
  ): Map<String, List<com.nixops.openapi.genai.model.Module>>
}

@Mapper(componentModel = "spring", uses = [ModuleMapper::class])
interface StudyProgramMapper {
  fun map(
      src: com.nixops.openapi.scraper.model.StudyProgram
  ): com.nixops.openapi.genai.model.StudyProgram
}
