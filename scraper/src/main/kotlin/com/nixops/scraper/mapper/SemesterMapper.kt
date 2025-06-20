package com.nixops.scraper.mapper

import com.nixops.scraper.model.Semester
import com.nixops.scraper.tum_api.nat.model.NatSemester
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface SemesterMapper {
  fun natSemesterToSemester(natSemester: NatSemester): Semester
}
