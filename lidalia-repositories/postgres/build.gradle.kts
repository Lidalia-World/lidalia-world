plugins {
  id("buildlogic.kotlin-library-conventions")
  alias(libs.plugins.ideaext)
}

dependencies {
  api(projects.lidaliaRepositoriesApi)
  testImplementation(libs.kotest.assertions.core)
}

idea {
  setPackagePrefix("uk.org.lidalia.repositories.postgres")
}
