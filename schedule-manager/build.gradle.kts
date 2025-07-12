plugins {
  java
  kotlin("jvm") version "1.9.25"
  kotlin("plugin.spring") version "1.9.25"
  id("org.springframework.boot") version "3.5.0"
  id("io.spring.dependency-management") version "1.1.7"
  id("org.openapi.generator") version "7.13.0"
}

group = "com.nixops"

version = "0.0.1"

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }

repositories {
  gradlePluginPortal()
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("com.squareup.okhttp3:okhttp:4.11.0")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

  // OpenAPI
  implementation("io.swagger.core.v3:swagger-core:2.2.20")
  implementation("io.swagger.core.v3:swagger-annotations:2.2.20")
  implementation("jakarta.validation:jakarta.validation-api:3.0.2")
  implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

  // OpenTelemetry
  implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter:2.17.1")
  implementation("org.springframework.boot:spring-boot-starter-actuator:3.5.3")

  // Caffeine Cache
  implementation("com.github.ben-manes.caffeine:caffeine:3.2.1")

  // Testing
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testImplementation("io.kotest:kotest-runner-junit5:5.7.2")
  testImplementation("io.kotest:kotest-assertions-core:5.7.2")
  testImplementation("io.kotest:kotest-framework-engine:5.7.2")
  testImplementation("io.mockk:mockk:1.14.4")
}

sourceSets {
  main {
    kotlin {
      srcDir(
          project.layout.buildDirectory
              .dir("generated/openapi-scraper/src/main/kotlin")
              .get()
              .asFile
              .path)
      srcDir(
          project.layout.buildDirectory
              .dir("generated/schedule-manager/src/main/kotlin")
              .get()
              .asFile
              .path)
    }
  }
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>(
    "openApiGenerateScraper") {
      generatorName.set("kotlin")

      val externalSpec = project.rootDir.resolve("../scraper/openapi.yaml")
      val internalSpec = project.file("scraper/openapi.yaml")

      val inputSpecPath =
          if (externalSpec.exists()) {
            externalSpec.absolutePath
          } else if (internalSpec.exists()) {
            internalSpec.absolutePath
          } else {
            throw GradleException(
                "Could not find scraper openapi.yaml in either external or internal locations.")
          }

      inputSpec.set(inputSpecPath)

      outputDir.set(
          project.layout.buildDirectory.dir("generated/openapi-scraper").get().asFile.path)
      packageName.set("com.nixops.openapi.scraper")
      apiPackage.set("com.nixops.openapi.scraper.api")
      modelPackage.set("com.nixops.openapi.scraper.model")
      configOptions.set(
          mapOf(
              "library" to "jvm-okhttp4",
              "dateLibrary" to "java8",
              "serializationLibrary" to "jackson",
              "testFramework" to "kotest",
              "modelMutable" to "true"))
    }

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>(
    "openApiGenerateScheduleManager") {
      generatorName.set("kotlin-spring")
      inputSpec.set(project.layout.projectDirectory.file("openapi.yaml").asFile.path)
      outputDir.set(
          project.layout.buildDirectory.dir("generated/schedule-manager").get().asFile.path)
      packageName.set("com.nixops.openapi.schedulemanager")
      apiPackage.set("com.nixops.openapi.schedulemanager.api")
      modelPackage.set("com.nixops.openapi.schedulemanager.model")
      configOptions.set(
          mapOf(
              "interfaceOnly" to "true",
              "library" to "spring-boot",
              "useSpringBoot3" to "true",
              "dateLibrary" to "java8",
              "serializationLibrary" to "jackson",
              "testFramework" to "kotest",
          ))
    }

tasks.named("compileKotlin") {
  dependsOn("openApiGenerateScraper", "openApiGenerateScheduleManager")
}
