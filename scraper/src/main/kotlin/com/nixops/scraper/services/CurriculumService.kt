package com.nixops.scraper.services

import com.nixops.scraper.mapper.CurriculumMapper
import com.nixops.scraper.model.Curriculum
import com.nixops.scraper.repository.CurriculumRepository
import com.nixops.scraper.tum_api.campus.api.CampusCurriculumApiClient
import com.nixops.scraper.tum_api.nat.api.NatSemesterApiClient
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class CurriculumService(
    private val curriculumRepository: CurriculumRepository,
    private val curriculumApiClient: CampusCurriculumApiClient,
    private val curriculumMapper: CurriculumMapper,
    //
    private val semesterService: SemesterService,
    private val semesterApiClient: NatSemesterApiClient,
    @PersistenceContext private val entityManager: EntityManager
) {
    fun getCurriculaBySemesterKey(semesterKey: String): List<Curriculum> {
        val semester = semesterApiClient.getSemester(semesterKey)
        val curricula = semester.semesterIdTumOnline?.let { curriculumApiClient.getCurriculaForSemester(it) }
            ?.map { curriculumMapper.natCurriculumToCurriculum(it) } ?: emptyList()

        return curricula
    }

    @Transactional
    fun saveCurricula(curricula: List<Curriculum>): List<Curriculum> {
        return curricula.map { curriculumRepository.save(it) }
    }

    fun getCurriculumByProgramName(semesterKey: String, name: String): Curriculum? {
        val curricula = getCurriculaBySemesterKey(semesterKey)
        // val savedCurricula = saveCurricula(curricula)

        return curricula.find { it.name == name }
    }

}