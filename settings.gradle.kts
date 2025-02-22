pluginManagement {
  repositories {
    gradlePluginPortal()
    maven {
      url = uri("https://public.mavenrepo.lidalia.org.uk/releases")
    }
  }
}

rootProject.name = "lidalia-world"

include(
  "lidalia-kotlin-lang",
  "lidalia-uri",
  "example-app",
)

include(
  "lidalia-encoding-core" to "./encoding/core",
  "lidalia-repositories-api" to "./lidalia-repositories/api",
  "lidalia-repositories-postgres" to "./lidalia-repositories/postgres",
  "lidalia-repositories-in-memory" to "./lidalia-repositories/in-memory",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

fun include(vararg namesToLocation: Pair<String, String>) {
  namesToLocation.forEach { (projectName, projectDir) ->
    include(":$projectName")
    project(":$projectName").projectDir = file(projectDir)
  }
}
