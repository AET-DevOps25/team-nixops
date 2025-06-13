package com.nixops.scraper.mapper

import com.nixops.scraper.model.StudyProgram
import com.nixops.scraper.tum_api.nat.model.NatProgram
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface StudyProgramMapper {
    fun natStudyProgramToStudyProgram(natStudyProgram: NatProgram): StudyProgram
}