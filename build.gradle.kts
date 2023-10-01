import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.gundy.semver4j.model.Version

plugins {
  base
  kotlin("jvm") apply false
  id("org.jmailen.kotlinter")
  alias(libs.plugins.dependency.analysis)
  alias(libs.plugins.task.tree)
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

tasks {
  check {
    dependsOn("buildHealth")
    dependsOn("installKotlinterPrePushHook")
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

fun String.isPreRelease(): Boolean = try {
  Version.fromString(this).preReleaseIdentifiers.isNotEmpty()
} catch (e: IllegalArgumentException) {
  false
}
