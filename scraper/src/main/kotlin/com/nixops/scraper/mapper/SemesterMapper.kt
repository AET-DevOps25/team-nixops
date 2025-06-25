package com.nixops.scraper.mapper

import com.nixops.scraper.model.Semester
import com.nixops.scraper.tum_api.nat.model.NatSemester
import org.springframework.stereotype.Component

@Component
class SemesterMapper {
  fun natSemesterToSemester(nat: NatSemester): Semester {
    return Semester.new(nat.semesterKey) {
      semesterTag = nat.semesterTag
      semesterTitle = nat.semesterTitle
      semesterIdTumOnline = nat.semesterIdTumOnline
    }
  }
}
