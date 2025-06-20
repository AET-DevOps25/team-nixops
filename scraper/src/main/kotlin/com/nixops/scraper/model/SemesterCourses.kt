package com.nixops.scraper.model

import jakarta.persistence.*

@Entity
@Table(name = "semester_courses")
data class SemesterCourses(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    var semester: String,
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinTable(
        name = "semester_course_course",
        joinColumns = [JoinColumn(name = "semester_course_id")],
        inverseJoinColumns = [JoinColumn(name = "course_id")])
    var courses: MutableSet<Course> = HashSet()
)
