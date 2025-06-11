package com.nixops.scraper.mapper

import com.nixops.openapi.model.ProgramBase
import com.nixops.scraper.model.Program
import org.mapstruct.Mapper

@Mapper(componentModel = "spring", uses = [DegreeMapper::class, SchoolMapper::class])
interface ProgramMapper {
    fun toEntity(dto: ProgramBase): Program
    fun toDto(entity: Program): ProgramBase
}