package com.nixops.scraper.model

import jakarta.persistence.*

@Entity
@Table(name = "curriculums")
data class Curriculum(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "curriculum_id")
    var id: Int = 0,
    @Column(name = "curriculum_name") var name: String = ""
)
