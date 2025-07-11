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

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("com.squareup.okhttp3:okhttp:4.11.0")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

  // Exposed
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.postgresql:postgresql:42.7.7")
  implementation("org.jetbrains.exposed:exposed-core:0.61.0")
  implementation("org.jetbrains.exposed:exposed-dao:0.61.0")
  implementation("org.jetbrains.exposed:exposed-jdbc:0.61.0")
  implementation("org.jetbrains.exposed:exposed-java-time:0.61.0")
  runtimeOnly("org.postgresql:postgresql:42.7.7")

  // OpenAPI
  implementation("io.swagger.core.v3:swagger-core:2.2.20")
  implementation("io.swagger.core.v3:swagger-annotations:2.2.20")
  implementation("jakarta.validation:jakarta.validation-api:3.0.2")
  implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")

  // OpenTelemetry
  implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter:2.17.1")
  implementation("org.springframework.boot:spring-boot-starter-actuator:3.5.3")

  // Testing
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testImplementation("io.kotest:kotest-runner-junit5:5.7.2")
  testImplementation("io.kotest:kotest-assertions-core:5.7.2")
  testImplementation("io.kotest:kotest-framework-engine:5.7.2")
  testImplementation("io.mockk:mockk:1.14.4")
  testImplementation("com.h2database:h2:2.2.224")
}

sourceSets {
  main {
    kotlin {
      srcDir(
          project.layout.buildDirectory.dir("generated/openapi/src/main/kotlin").get().asFile.path)
    }
  }
}

tasks.openApiGenerate {
  generatorName.set("kotlin-spring")
  inputSpec.set(project.layout.projectDirectory.file("openapi.yaml").asFile.path)
  outputDir.set(project.layout.buildDirectory.dir("generated/openapi").get().asFile.path)
  packageName.set("com.nixops.openapi")
  apiPackage.set("com.nixops.openapi.api")
  modelPackage.set("com.nixops.openapi.model")
  configOptions.set(
      mapOf(
          "interfaceOnly" to "true",
          "library" to "spring-boot",
          "useSpringBoot3" to "true",
          "dateLibrary" to "java8",
          "serializationLibrary" to "jackson",
          "testFramework" to "kotest",
      ))
  generateModelTests.set(false)
  generateApiTests.set(false)
  generateModelDocumentation.set(false)
  generateApiDocumentation.set(false)
  skipValidateSpec.set(true)
}

tasks.named("compileKotlin") { dependsOn("openApiGenerate") }

tasks.test { useJUnitPlatform { excludeTags("remoteApi") } }

tasks.register<Test>("remoteApiTest") {
  useJUnitPlatform { includeTags("remoteApi") }
  testClassesDirs = sourceSets.test.get().output.classesDirs
  classpath = sourceSets.test.get().runtimeClasspath
}
