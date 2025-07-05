package com.nixops.schedulingEngine.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.nixops.schedulingEngine.engine.SchedulingEngine
import com.nixops.schedulingEngine.model.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest(SchedulingController::class)
class SchedulingControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {

  @MockBean lateinit var schedulingEngine: SchedulingEngine

  @Test
  fun `should create schedule and return response`() {
    val query =
        Query(
            courses =
                listOf(
                    Course(
                        events =
                            listOf(
                                Event(
                                    title = "Math 101",
                                    type = "Lecture",
                                    timeSlot =
                                        TimeSlot(
                                            date = LocalDate.now(),
                                            startTime = LocalTime.of(9, 0),
                                            endTime = LocalTime.of(11, 0)))),
                        score = 5)),
            ectsTarget = 30)

    val expectedResponse =
        Response(
            scheduled =
                Schedule(
                    events = query.courses.flatMap { it.events },
                    score = 100,
                    collisions =
                        listOf(
                            Collision(
                                primary = query.courses[0].events[0],
                                secondary = query.courses[0].events[0],
                                duration = Duration.ofHours(2)))),
            complement = emptyList())

    whenever(schedulingEngine.createSchedule(query)).thenReturn(expectedResponse)

    mockMvc
        .post("/api/schedule") {
          contentType = MediaType.APPLICATION_JSON
          content = objectMapper.writeValueAsString(query)
        }
        .andExpect {
          status { isOk() }
          content { contentType(MediaType.APPLICATION_JSON) }
          jsonPath("$.scheduled.score").value(100)
          jsonPath("$.scheduled.events[0].title").value("Math 101")
        }
  }
}
