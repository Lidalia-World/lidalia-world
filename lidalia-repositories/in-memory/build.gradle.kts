plugins {
  id("buildlogic.kotlin-library-conventions")
  alias(libs.plugins.ideaext)
}

dependencies {
  api(projects.lidaliaRepositoriesApi)
  api(libs.arrow.core)
  testImplementation(testFixtures(projects.lidaliaRepositoriesApi))
  testImplementation(libs.kotest.assertions.core)
}

idea {
  setPackagePrefix("uk.org.lidalia.repositories.inmemory")
}
