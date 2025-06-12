package com.nixops.scraper

import com.nixops.scraper.services.ModuleService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.*

import com.nixops.scraper.tum_api.campus.api.CampusCourseApiClient
import com.nixops.scraper.tum_api.campus.api.CampusCurriculumApiClient
import com.nixops.scraper.tum_api.nat.api.NatCourseApiClient
import com.nixops.scraper.tum_api.nat.api.NatProgramApiClient
import com.nixops.scraper.tum_api.nat.api.NatSemesterApiClient
import org.springframework.transaction.annotation.Transactional

@SpringBootApplication
@RestController
class ScraperApplication(
    private val curriculumClient: CampusCurriculumApiClient,
    private val campusCourseClient: CampusCourseApiClient,
    private val semesterClient: NatSemesterApiClient,
    private val programClient: NatProgramApiClient,
    private val courseClient: NatCourseApiClient,
    private val moduleService: ModuleService
) {
    @Transactional
    @GetMapping("/hello")
    fun hello(@RequestParam(value = "name", defaultValue = "World") name: String): String {
        try {
            // 1. Fetch current semester (lecture)
            val semester = semesterClient.getCurrentLectureSemester()
            println("Semester:")
            println("semester title: ${semester.semesterTitle}")
            println("semester tag: ${semester.semesterTag}")
            println("semester key: ${semester.semesterKey}")
            println("semester id: ${semester.semesterIdTumOnline}")
            println()

            // 2. Fetch curricula from campus API
            val curricula = curriculumClient.getCurriculaForSemester(semester.semesterIdTumOnline);

            // 3. Search Programs
            val query = "M.Sc. Informatik"
            val spo = "20231"
            var selectedCurriculumId: Int? = null
            var schoolOrgId: Int? = null
            var orgId: Int? = null

            println("Study Program:")
            val programs = programClient.searchPrograms(query)
            for (program in programs) {
                println("program: ${program.degreeProgramName}")
                println("study id: ${program.studyId}")
                println("org id: ${program.orgId}")
                println("school org id: ${program.school.orgId}")
                println("spo version: ${program.spoVersion}")

                if (program.spoVersion != spo) {
                    println("wrong spo")
                    println()
                    continue
                }

                val longName = "${program.programName} [${program.spoVersion}], ${program.degree.degreeTypeName}"
                println("long name: $longName")

                val matchedCurriculum = curricula.find { curriculum ->
                    curriculum.name == longName
                }

                if (matchedCurriculum == null) {
                    println("no curriculum available")
                    println()
                    continue
                }

                selectedCurriculumId = matchedCurriculum.id
                schoolOrgId = program.school.orgId
                orgId = program.orgId
                println("curriculum id: $selectedCurriculumId")
                println()
                break // Assuming you want only the first match
            }

            if (selectedCurriculumId == null || schoolOrgId == null || orgId == null) {
                println("No suitable program and curriculum found, aborting.")
                return "Abort"
            }

            // 4. Fetch Modules for school org id
            println("Fetch Modules: $schoolOrgId")
            /*
            val modules = moduleClient.fetchAllNatModulesWithDetails(schoolOrgId)

            println("Modules:")
            val extraModuleMapping = mutableMapOf<Int, MutableList<Int>>() // courseId -> List<moduleId>

            for (module in modules) {
                // Fetch full module details

                // Remove exams (if needed, depends on your model)
                // In Kotlin, just ignore or don't use exams

                if (module.courses?.contains(semester.semesterKey) == true) {
                    for (course in module.courses[semester.semesterKey]!!) {
                        val courseId = course.courseId
                        module.moduleId?.let { extraModuleMapping.getOrPut(courseId) { mutableListOf() }.add(it) }
                    }
                }
            }
            println()
            */

            println("Modules:")
            val modules = moduleService.getModules();
            val extraModuleMapping = mutableMapOf<Int, MutableList<Int>>() // courseId -> List<moduleId>
            for (module in modules) {
                // Fetch full module details

                // Remove exams (if needed, depends on your model)
                // In Kotlin, just ignore or don't use exams

                for (semesterCourses in module.semesterCourses) {
                    if (semesterCourses.semester != semester.semesterKey) {
                        continue;
                    }

                    for (course in semesterCourses.courses) {
                        val courseId = course.id
                        module.moduleId?.let { extraModuleMapping.getOrPut(courseId) { mutableListOf() }.add(it) }
                    }

                    break;
                }
            }
            println()

            // 5. Fetch Courses with paging
            println("Fetch Courses:")
            val courses = campusCourseClient.getCourses(selectedCurriculumId, semester.semesterIdTumOnline);

            // 6. Analyze courses for modules
            println("Courses:")

            var num = 0;
            for (course in courses) {
                val detailedCourse = courseClient.getCourseById(course.id)

                println("course: ${course.id} ${course.courseTitle?.value}")

                val moduleIds = if (detailedCourse.modules.isNotEmpty()) {
                    detailedCourse.modules.map { it.moduleId }
                } else if (extraModuleMapping.containsKey(course.id)) {
                    extraModuleMapping[course.id]
                } else {
                    println("No module found :( {${detailedCourse.org?.orgId}}")
                    num += 1;
                    listOf()
                }

                if (moduleIds != null) {
                    for (moduleId in moduleIds) {
                        val module = moduleId?.let { moduleService.getModuleById(it) }
                        if (module != null) {
                            println("module: ${module.moduleTitle}")
                        } else {
                            println("could not find module with id: $moduleId")
                        }
                    }
                }

                println()
            }
            println("No module found for $num courses")
        } catch (e: Exception) {
            println("An error occurred: ${e.message}")
        }


        return "Hallo Welt"
    }
}

fun main(args: Array<String>) {
    runApplication<ScraperApplication>(*args)
}
