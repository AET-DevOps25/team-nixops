package com.nixops.scraper.model

import com.nixops.scraper.tum_api.nat.model.NatDegree
import com.nixops.scraper.tum_api.nat.model.NatSchool
import jakarta.persistence.*

@Entity
@Table(name = "study_programs")
data class StudyProgram(
    @Id
    @Column(name = "study_id")
    val studyId: Int,

    @Column(name = "org_id")
    val orgId: Int,

    @Column(name = "spo_version")
    val spoVersion: String,

    @Column(name = "program_name")
    val programName: String,

    @Column(name = "degree_program_name")
    val degreeProgramName: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_id", referencedColumnName = "id")
    val degree: Degree
)