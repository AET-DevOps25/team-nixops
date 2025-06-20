package com.nixops.scraper.model

import jakarta.persistence.*

@Entity
@Table(name = "semester")
data class Semester(
    @Id @Column(name = "semester_key") var semesterKey: String,
    @Column(name = "semester_tag") var semesterTag: String,
    @Column(name = "semester_title") var semesterTitle: String,
    @Column(name = "semester_id_tumonline") var semesterIdTumOnline: Int?,
)
