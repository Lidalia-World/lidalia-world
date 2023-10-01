import org.gradle.accessors.dm.LibrariesForLibs

plugins {
  // Apply the common convention plugin for shared build configuration between library and application projects.
  id("wiremock.http4k.kotlin-common-conventions")

  // Apply the java-library plugin for API and implementation separation.
  `java-library`
}

val libs = the<LibrariesForLibs>()

dependencies {
  testImplementation(platform(libs.kotest.bom))

  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.framework.api)
  testImplementation(libs.kotest.assertions.shared)
}

tasks {
  test {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
    systemProperty("kotest.framework.classpath.scanning.config.disable", "true")
    systemProperty("kotest.framework.classpath.scanning.autoscan.disable", "true")
  }
}
