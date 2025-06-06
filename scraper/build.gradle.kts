plugins {
  java
  kotlin("jvm") version "1.9.25"
  kotlin("plugin.spring") version "1.9.25"
  kotlin("plugin.jpa") version "1.9.25"
  id("org.springframework.boot") version "3.5.0"
  id("io.spring.dependency-management") version "1.1.7"
  id("org.openapi.generator") version "7.13.0"
}

group = "com.nixops"
version = "0.0.1"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

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
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  runtimeOnly("org.postgresql:postgresql")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testImplementation("io.kotest:kotest-runner-junit5:5.7.2")
  testImplementation("io.kotest:kotest-assertions-core:5.7.2")
  testImplementation("io.kotest:kotest-framework-engine:5.7.2")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict")
  }
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set(project.layout.projectDirectory.file("src/main/resources/openapi.json").asFile.path)
    outputDir.set(project.layout.buildDirectory.dir("generated").get().asFile.path)
    apiPackage.set("com.example.api")
    modelPackage.set("com.example.model")
    invokerPackage.set("com.example.invoker")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "serializationLibrary" to "jackson",
            "testFramework" to "kotest"
        )
    )
    generateModelTests.set(false)
    generateApiTests.set(false)
    generateModelDocumentation.set(false)
    generateApiDocumentation.set(false)
    validateSpec.set(false)
}

sourceSets {
    main {
        kotlin {
            srcDir(project.layout.buildDirectory.dir("generated/src/main/kotlin").get().asFile.path)
        }
    }
}

tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
}

tasks.test {
    useJUnitPlatform()
}
