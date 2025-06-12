package com.nixops.scraper.mapper

import com.nixops.openapi.model.SchoolBase
import com.nixops.scraper.model.School
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface SchoolMapper {
    fun toEntity(dto: SchoolBase): School
    fun toDto(entity: School): SchoolBase
}
