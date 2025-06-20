package com.nixops.scraper.mapper

import com.nixops.scraper.model.Curriculum
import com.nixops.scraper.tum_api.campus.model.CampusCurriculum
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface CurriculumMapper {
  fun natCurriculumToCurriculum(natCurriculum: CampusCurriculum): Curriculum
}
