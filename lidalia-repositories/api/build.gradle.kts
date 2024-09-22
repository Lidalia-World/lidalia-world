plugins {
  id("lidalia.world.kotlin-library-conventions")
  `java-test-fixtures`
  alias(libs.plugins.ideaext)
}

dependencies {
  api(platform(libs.arrow.stack))
  api(libs.arrow.core)
  testFixturesImplementation(platform(libs.kotest.bom))
  testFixturesImplementation(projects.lidaliaRepositoriesInMemory)
  testFixturesImplementation(libs.kotest.runner.junit5)
  testFixturesImplementation(libs.kotest.framework.api)
  testFixturesImplementation(libs.kotest.assertions.shared)
}

idea {
  setPackagePrefix("uk.org.lidalia.repositories.api")
}
