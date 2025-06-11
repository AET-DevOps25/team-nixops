package com.nixops.scraper.mapper

import com.nixops.openapi.model.DegreeBase
import com.nixops.scraper.model.Degree
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface DegreeMapper {
    fun toEntity(dto: DegreeBase): Degree
    fun toDto(entity: Degree): DegreeBase
}