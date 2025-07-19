package com.nixops.schedulemanager.controller

import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class OpenApiSpecController {

  @RequestMapping("/api-docs.yaml", produces = ["application/yaml"])
  fun getOpenApiYaml(): ResponseEntity<Resource> {
    val resource = ClassPathResource("openapi.yaml")
    return if (resource.exists()) {
      ResponseEntity.ok(resource)
    } else {
      ResponseEntity.notFound().build()
    }
  }
}
