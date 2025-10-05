pluginManagement {
  repositories {
    gradlePluginPortal()
    maven {
      url = uri("https://public.mavenrepo.lidalia.org.uk/releases")
    }
  }
  includeBuild("gradle/build-logic")
}

plugins {
  id("com.autonomousapps.build-health") version "3.0.4"

  // Kotlin must be loaded in the same (or parent) class loader as the
  // Dependency Analysis Plugin. The lines below are one way to accomplish this
  id("org.jetbrains.kotlin.jvm") version "2.2.20" apply false
}

rootProject.name = "lidalia-world"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
  "lidalia-kotlin-lang",
  "lidalia-kotest",
  "lidalia-uri",
  "example-app",
)

include(
  "lidalia-encoding-core" to "./encoding/core",
  "lidalia-encoding-hex" to "./encoding/hex",
  "lidalia-repositories-api" to "./lidalia-repositories/api",
  "lidalia-repositories-postgres" to "./lidalia-repositories/postgres",
  "lidalia-repositories-in-memory" to "./lidalia-repositories/in-memory",
)

fun include(vararg namesToLocation: Pair<String, String>) {
  namesToLocation.forEach { (projectName, projectDir) ->
    include(":$projectName")
    project(":$projectName").projectDir = file(projectDir)
  }
}
