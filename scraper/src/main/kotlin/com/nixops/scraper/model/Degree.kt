package com.nixops.scraper.model

import jakarta.persistence.*

@Entity
@Table(name = "degree")
data class Degree(
    @Id
    @Column(name = "degree_type_id")
    var degreeTypeId: Int = 0,

    @Column(name = "degree_type_name", nullable = false)
    var degreeTypeName: String = "",

    @Column(name = "degree_type_short")
    var degreeTypeShort: String? = null,

    @Column(name = "program_type_name")
    var programTypeName: String? = null,

    @Column(name = "program_type_name_en")
    var programTypeNameEn: String? = null
)
