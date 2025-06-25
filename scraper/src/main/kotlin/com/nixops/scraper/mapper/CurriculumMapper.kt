package com.nixops.scraper.mapper

import com.nixops.scraper.model.Curriculum
import com.nixops.scraper.tum_api.campus.model.CampusCurriculum
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Component

@Component
class CurriculumMapper {

  fun natCurriculumToCurriculum(nat: CampusCurriculum): Curriculum = transaction {
    Curriculum.new(nat.id) { name = nat.name }
  }
}
