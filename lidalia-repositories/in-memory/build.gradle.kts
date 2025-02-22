plugins {
  id("buildlogic.kotlin-library-conventions")
  alias(libs.plugins.ideaext)
}

dependencies {
  api(projects.lidaliaRepositoriesApi)
  api(libs.arrow.core)
  testImplementation(testFixtures(projects.lidaliaRepositoriesApi))
}

idea {
  setPackagePrefix("uk.org.lidalia.repositories.inmemory")
}
