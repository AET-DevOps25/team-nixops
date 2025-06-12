package com.nixops.scraper.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*

@Entity
@Table(name= "courses")
class Course(
    @Id
    @Column(name = "course_id")
    var id: Int,

    @Column(name = "course_name")
    var courseName: String,

    @Column(name = "course_name_en")
    var courseNameEn: String,

    @Column(name = "course_name_list")
    var courseNameList: String,

    @Column(name = "course_name_list_en")
    var courseNameListEn: String,

    @ElementCollection
    @CollectionTable(
        name = "course_instruction_languages",
        joinColumns = [JoinColumn(name = "course_id")]
    )
    @Column(name = "instruction_language")
    var instructionLanguages: MutableList<String>? = null,

    @Lob
    @Column(name = "description")
    var description: String? = null,

    @Lob
    @Column(name = "description_en")
    var descriptionEn: String? = null,

    @Column(name = "teaching_method")
    var teachingMethod: String? = null,

    @Column(name = "teaching_method_en")
    var teachingMethodEn: String? = null,

    @Column(name = "note")
    var note: String? = null,

    @Column(name = "note_en")
    var noteEn: String? = null,

    // @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    // var semesterCourses: Set<SemesterCourses> = HashSet()
)