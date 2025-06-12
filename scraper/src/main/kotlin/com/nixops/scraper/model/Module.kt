package com.nixops.scraper.model

import jakarta.persistence.*

@Entity
@Table(name = "modules")
data class Module(
    @Id
    @Column(name = "module_id")
    val moduleId: Int? = null,

    @Column(name = "module_title")
    val moduleTitle: String? = null,

    @Column(name = "module_code")
    val moduleCode: String? = null,

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinTable(
        name = "module_course",
        joinColumns = [JoinColumn(name = "module_id")],
        inverseJoinColumns = [JoinColumn(name = "course_id")]
    )
    val courses: Set<Course> = emptySet()
)