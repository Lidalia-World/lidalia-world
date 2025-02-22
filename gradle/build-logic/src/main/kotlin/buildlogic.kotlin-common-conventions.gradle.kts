import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Path

plugins {
  id("org.jmailen.kotlinter")
  id("org.jetbrains.kotlin.jvm") apply false
  `project-report`
}

repositories {
  mavenCentral()
}

val libs = the<LibrariesForLibs>()

dependencies {
  implementation(platform(libs.arrow.stack))
  implementation(platform(libs.kotest.bom))
  implementation(platform(libs.kotlinx.coroutines.bom))

  if (project.name != "lidalia-kotlin-lang") {
    api(project(":lidalia-kotlin-lang"))
  }

  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.framework.api)
  testImplementation(libs.kotest.framework.engine)
  testImplementation(libs.kotest.assertions.api)
  testImplementation(libs.kotest.assertions.shared)
  testImplementation(libs.kotest.common)
  testImplementation(libs.kotest.extensions.jvm)
  testImplementation(libs.kotest.extensions.now)

  if (project.name != "lidalia-kotest") {
    testImplementation(project(":lidalia-kotest"))
  }

  testRuntimeOnly(libs.slf4j.simple)

  modules {
    module(libs.slf4j.nop.get().module) {
      replacedBy(libs.logback.classic.get().module)
    }
    module(libs.slf4j.simple.get().module) {
      replacedBy(libs.logback.classic.get().module)
    }
    module(libs.slf4j.jdk14.get().module) {
      replacedBy(libs.logback.classic.get().module)
    }
    module(libs.log4j2.core.get().module) {
      replacedBy(libs.log4j2.to.slf4j.get().module)
    }
    module(libs.commons.logging.get().module) {
      replacedBy(libs.slf4j.jcl.over.slf4j.get().module)
    }
    module(libs.log4j.get().module) {
      replacedBy(libs.slf4j.log4j.over.slf4j.get().module)
    }
  }
}

configurations.testRuntimeClasspath {
  exclude(libs.slf4j.nop.get().module.group, libs.slf4j.nop.get().module.name)
}

java {
  sourceCompatibility = VERSION_21
  targetCompatibility = VERSION_21
}

tasks.withType<KotlinCompile> {
  compilerOptions.jvmTarget = JVM_21
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

layout.buildDirectory = rootProject.layout.buildDirectory.childProjectPath()

tasks.dependencyReport {
  projectReportDirectory = rootProject.tasks
    .dependencyReport
    .flatMap { it.projectReportDirectory.childProjectPath() }
}

dependencyLocking {
  lockAllConfigurations()
}

val relativeProjectPath: Path = rootProject.projectDir.toPath().relativize(projectDir.toPath())

fun DirectoryProperty.childProjectPath(): Provider<Directory> = map {
  it.dir("child-projects").dir(relativeProjectPath.toString())
}
