package com.nixops.scraper.model

import jakarta.persistence.*

@Entity
@Table(name = "modules")
data class Module(
    @Id
    @Column(name = "module_id")
    var moduleId: Int? = null,

    @Column(name = "module_title")
    var moduleTitle: String? = null,

    @Column(name = "module_code")
    var moduleCode: String? = null,

    @OneToMany(mappedBy = "module", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var semesterCourses: List<SemesterCourses> = emptyList()
)