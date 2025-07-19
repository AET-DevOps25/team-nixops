plugins {
  java
  kotlin("jvm") version "1.9.25"
  kotlin("plugin.spring") version "1.9.25"
  id("org.springframework.boot") version "3.5.0"
  id("io.spring.dependency-management") version "1.1.7"
  id("org.openapi.generator") version "7.13.0"
}

group = "com.nixops"

version = "0.0.1-SNAPSHOT"

val base = "$group.scheduleOptimizer"

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

repositories { mavenCentral() }

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.webjars:webjars-locator-core")
  // implementation("org.choco-solver:choco-solver:4.10.14")

  implementation("com.google.code.findbugs:jsr305:3.0.2")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("jakarta.validation:jakarta.validation-api")
  implementation("jakarta.annotation:jakarta.annotation-api:2.1.0")

  runtimeOnly("org.webjars:swagger-ui:5.25.3")

  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(module = "junit")
  }
}

sourceSets {
  main {
    kotlin { srcDir(layout.buildDirectory.dir("generated/src/main/kotlin")) }
    resources { srcDir(layout.buildDirectory.dir("generated/src/main/resources")) }
  }
}

tasks.openApiGenerate {
  generatorName.set("kotlin-spring")
  inputSpec.set("$rootDir/openapi.yaml")
  outputDir.set(layout.buildDirectory.dir("generated").get().asFile.path)
  apiPackage.set("$base.api")
  modelPackage.set("$base.model")
  groupId.set(group)
  cleanupOutput.set(true)
  configOptions.set(
      mapOf(
          // "interfaceOnly" to "true"useBeanValidation" to "true",",
          "delegatePattern" to "true",
          "apiSuffix" to "",
          "useSpringBoot3" to "true",
          "dateLibrary" to "java8",
          "serializationLibrary" to "jackson",
          "testFramework" to "kotest",
          "useBeanValidation" to "true",
          "documentationProvider" to "source"))
}

tasks.test { useJUnitPlatform() }

tasks.named("compileKotlin") { dependsOn("openApiGenerate") }

tasks.named("processResources") { dependsOn("openApiGenerate") }

kotlin { compilerOptions { freeCompilerArgs.addAll(listOf("-Xjsr305=strict")) } }
