@file:Suppress("UnstableApiUsage")

import com.github.gundy.semver4j.model.Version

plugins {
  base
  `project-report`
  alias(libs.plugins.kotlinter)
  alias(libs.plugins.taskTree)
  alias(libs.plugins.versions)
}

buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath(libs.semver4j)
  }
}

val desiredArtifactDeps = configurations.dependencyScope("desiredArtifactDeps")

val desiredArtifacts = configurations.resolvable("desiredArtifacts") {
  extendsFrom(desiredArtifactDeps.get())
  isTransitive = false
}

dependencies {
  subprojects.forEach { project ->
    desiredArtifactDeps(project(project.path))
  }
}

tasks {
  check {
    dependsOn(buildHealth)
    dependsOn("installKotlinterPrePushHook")
  }

  val copyArtifacts by registering(Sync::class) {
    from(desiredArtifacts)
    into(layout.buildDirectory.dir("artifacts"))
  }

  assemble {
    dependsOn(copyArtifacts)
  }

  dependencyUpdates {
    rejectVersionIf {
      candidate.version.isPreRelease()
    }
  }
}

dependencyAnalysis {
  issues {
    // configure for all projects
    all {
      // set behavior for all issue types
      onAny {
        severity("fail")
        exclude(
          projects.lidaliaKotlinLang.path,
        )
      }
      onDuplicateClassWarnings {
        severity("fail")
      }
      sourceSet("test") {
        onUnusedDependencies {
          exclude(
            libs.kotest.runner.junit5,
            libs.kotest.framework.api,
            libs.kotest.framework.engine,
            libs.kotest.assertions.api,
            libs.kotest.assertions.core,
            libs.kotest.assertions.shared,
            libs.kotest.common,
            libs.kotest.extensions.jvm,
            libs.kotest.extensions.now,
          )
          exclude(
            projects.lidaliaKotest.path,
          )
        }
      }
    }
  }
}

fun String.isPreRelease(): Boolean = try {
  Version.fromString(this).preReleaseIdentifiers.isNotEmpty()
} catch (_: IllegalArgumentException) {
  false
}
