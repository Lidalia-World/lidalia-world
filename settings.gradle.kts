plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "lidalia-world"

include("lidalia-kotlin-lang")
include("lidalia-uri")

fun Settings.createProject(projectDir: String, projectName: String) {
  include(projectName)
  project(projectName).projectDir = projectDir.toFile()
}

fun String.toFile() = File(this)
