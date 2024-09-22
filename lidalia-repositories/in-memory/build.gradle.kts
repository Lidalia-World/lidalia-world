plugins {
  id("lidalia.world.kotlin-library-conventions")
  alias(libs.plugins.ideaext)
}

dependencies {
  api(projects.lidaliaRepositoriesApi)
  testImplementation(testFixtures(projects.lidaliaRepositoriesApi))
}

idea {
  setPackagePrefix("uk.org.lidalia.repositories.inmemory")
}
