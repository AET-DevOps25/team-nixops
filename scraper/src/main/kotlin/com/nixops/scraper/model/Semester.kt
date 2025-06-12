package com.nixops.scraper.model

import jakarta.persistence.*

@Entity
@Table(name = "semester")
class Semester (
    @Id
    @Column(name = "semester_key")
    val semesterKey: String,

    @Column(name = "semester_tag")
    val semesterTag: String,

    @Column(name = "semester_title")
    val semesterTitle: String
)