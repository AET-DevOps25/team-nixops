package com.nixops.scraper.services

import com.nixops.scraper.mapper.SemesterMapper
import com.nixops.scraper.model.Semester
import com.nixops.scraper.tum_api.nat.api.NatSemesterApiClient
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class SemesterService(
    private val semesterApiClient: NatSemesterApiClient,
    private val semesterMapper: SemesterMapper,
) {

  fun getCurrentLectureSemester(): Semester = getSemester("lecture")

  fun getSemester(semesterKey: String): Semester {
    return transaction {
      val existing = Semester.findById(semesterKey)
      if (existing != null) {
        existing
      } else {
        val natSemester = semesterApiClient.getSemester(semesterKey)
        println("Saving semester with key: ${natSemester.semesterKey}")

        val semester = semesterMapper.natSemesterToSemester(natSemester)

        println("Saved semester with key: $Semester")
        semester
      }
    }
  }

  fun getSemesters(): List<Semester> {
    val semesters = semesterApiClient.getSemesters()
    return semesters.map { getSemester(it.semesterKey) }
  }
}
