pluginManagement {
  repositories {
    gradlePluginPortal()
    maven {
      url = uri("https://public.mavenrepo.lidalia.org.uk/releases")
    }
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "lidalia-world"

include("lidalia-kotlin-lang")
include("lidalia-uri")
include("lidalia-repositories")
createProject("./encoding/core", "lidalia-encoding-core")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

fun createProject(projectDir: String, projectName: String) {
  include(":$projectName")
  project(":$projectName").projectDir = file(projectDir)
}

fun String.toFile() = File(this)
