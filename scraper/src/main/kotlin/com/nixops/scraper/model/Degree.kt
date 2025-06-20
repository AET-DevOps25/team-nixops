package com.nixops.scraper.model

import jakarta.persistence.*

@Entity
@Table(name = "degrees")
data class Degree(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    @Column(name = "degree_type_name") val degreeTypeName: String
)
