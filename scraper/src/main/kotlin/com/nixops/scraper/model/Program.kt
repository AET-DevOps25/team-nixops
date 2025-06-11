package com.nixops.scraper.model

import jakarta.persistence.*
import java.net.URI

@Entity
@Table(name = "programs")
data class Program(
    @Id
    @Column(name = "study_id")
    var studyId: String = "",

    @Column(name = "program_name", nullable = false)
    var programName: String = "",

    @Column(name = "program_name_en", nullable = false)
    var programNameEn: String = "",

    @Column(name = "degree_program_name", nullable = false)
    var degreeProgramName: String = "",

    @Column(name = "degree_program_name_en", nullable = false)
    var degreeProgramNameEn: String = "",

    @Column(name = "org_id", nullable = false)
    var orgId: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "degree_id", nullable = false)
    var degree: Degree,

    @Column(name = "program_short")
    var programShort: String? = null,

    @Column(name = "program_url")
    var programUrl: URI? = null,

    @Column(name = "program_url_en")
    var programUrlEn: URI? = null,

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "school_id")
    var school: School? = null
)
