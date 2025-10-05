plugins {
  id("buildlogic.kotlin-library-conventions")
  alias(libs.plugins.ideaext)
}

dependencies {
  api(platform(libs.arrow.stack))
  api(libs.arrow.core)
  api(projects.lidaliaRepositoriesApi)
  api(projects.lidaliaUri)
  testImplementation(libs.kotest.assertions.core)
}

idea {
  setPackagePrefix("uk.org.lidalia.example")
}
