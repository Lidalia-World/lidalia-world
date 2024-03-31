import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.gundy.semver4j.model.Version
import java.nio.file.Path

plugins {
  base
  kotlin("jvm") apply false
  id("org.jmailen.kotlinter")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.taskTree)
  alias(libs.plugins.versions)
}

buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath(group = "com.github.gundy", name = "semver4j", version = "0.16.4")
  }
}

val artifacts: Configuration by configurations.creating {
  isTransitive = false
}

dependencies {
  subprojects.forEach { project ->
    artifacts(project(project.path))
  }
}

tasks {
  check {
    dependsOn("buildHealth")
    dependsOn("installKotlinterPrePushHook")
  }

  val copyArtifacts by registering(Copy::class) {
    from(artifacts)
    into(layout.buildDirectory.dir("artifacts"))
  }

  assemble {
    dependsOn(copyArtifacts)
  }
}

dependencyAnalysis {
  issues {
    // configure for all projects
    all {
      // set behavior for all issue types
      onAny {
        severity("fail")
      }
    }
  }
}

tasks.withType<DependencyUpdatesTask> {
  rejectVersionIf {
    candidate.version.isPreRelease()
  }
}

subprojects {
  val relativeProjectPath: Path = rootProject.projectDir.toPath().relativize(projectDir.toPath())
  layout.buildDirectory = rootProject.layout.buildDirectory.get()
    .dir("child-projects")
    .dir(relativeProjectPath.toString())
}

fun String.isPreRelease(): Boolean = try {
  Version.fromString(this).preReleaseIdentifiers.isNotEmpty()
} catch (e: IllegalArgumentException) {
  false
}
