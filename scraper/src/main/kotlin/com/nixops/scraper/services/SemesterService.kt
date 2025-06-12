package com.nixops.scraper.services

import com.nixops.scraper.mapper.SemesterMapper
import com.nixops.scraper.model.Semester
import com.nixops.scraper.repository.SemesterRepository
import com.nixops.scraper.tum_api.nat.api.NatSemesterApiClient
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class SemesterService(
    private val semesterRepository: SemesterRepository,
    private val semesterApiClient: NatSemesterApiClient,
    private val semesterMapper: SemesterMapper
) {
    @Transactional
    fun getCurrentLectureSemester(): Semester {
        return getSemester("lecture")
    }

    @Transactional
    fun getSemester(semesterKey: String): Semester {
        val semester = semesterRepository.findBySemesterKey(semesterKey)
        if (semester != null) {
            return semester
        } else {
            val natSemester = semesterApiClient.getSemester(semesterKey)

            println("Saving semester with key: ${natSemester.semesterKey}")

            val newSemester = semesterMapper.natSemesterToSemester(natSemester)

            println("Saving semester with key: ${newSemester.semesterKey}")
            return semesterRepository.save(newSemester)
        }
    }

    @Transactional
    fun getSemesters(): List<Semester> {
        val semesters = semesterApiClient.getSemesters()
        return semesters.map { getSemester(it.semesterKey) }
    }
}