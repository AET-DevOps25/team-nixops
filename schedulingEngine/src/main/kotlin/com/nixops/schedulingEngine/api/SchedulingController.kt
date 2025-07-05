package com.nixops.schedulingEngine.api

import com.nixops.schedulingEngine.engine.SchedulingEngine
import com.nixops.schedulingEngine.model.Query
import com.nixops.schedulingEngine.model.Response
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class SchedulingController(private val schedulingEngine: SchedulingEngine) {

  @PostMapping("/schedule")
  @Operation(
      summary = "Create a schedule",
      description =
          "Generates a schedule given a set of courses, availability blockers, and ECTS target.",
      requestBody =
          SwaggerRequestBody(
              required = true, content = [Content(schema = Schema(implementation = Query::class))]),
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "Successfully generated schedule",
                  content = [Content(schema = Schema(implementation = Response::class))])])
  fun createSchedule(@RequestBody query: Query): ResponseEntity<Response> {
    val response = schedulingEngine.createSchedule(query)
    return ResponseEntity.ok(response)
  }
}
