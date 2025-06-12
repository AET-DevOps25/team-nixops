package com.nixops.scraper.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*

@Entity
@Table(name= "courses")
class Course(
    @Id
    @Column(name = "course_id")
    val id: Long,

    @Column(name = "course_name")
    val courseName: String,

    @Column(name = "course_name_en")
    val courseNameEn: String,

    @Column(name = "course_name_list")
    val courseNameList: String,

    @Column(name = "course_name_list_en")
    val courseNameListEn: String,

    @ElementCollection
    @CollectionTable(
        name = "course_instruction_languages",
        joinColumns = [JoinColumn(name = "course_id")]
    )
    @Column(name = "instruction_language")
    val instructionLanguages: List<String> = listOf(),

    @Column(name = "description")
    val description: String,

    @Column(name = "description_en")
    val descriptionEn: String,

    @Column(name = "teaching_method")
    val teachingMethod: String,

    @Column(name = "teaching_method_en")
    val teachingMethodEn: String,

    @Column(name = "note")
    val note: String,

    @Column(name = "note_en")
    val noteEn: String,
    
    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    val modules: Set<Module> = HashSet()
)