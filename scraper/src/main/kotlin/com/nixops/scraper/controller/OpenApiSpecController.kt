package com.nixops.scraper.controller

import java.io.File
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class OpenApiSpecController {

  @RequestMapping("/api-docs.yaml", produces = ["application/yaml"])
  fun getOpenApiYaml(): Resource {
    val file = File("openapi.yaml")
    println("Trying to serve: ${file.absolutePath} exists: ${file.exists()}")
    return FileSystemResource(file)
  }
}
