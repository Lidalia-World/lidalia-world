plugins {
  id("buildlogic.kotlin-library-conventions")
  alias(libs.plugins.ideaext)
}

dependencies {
  api(projects.lidaliaRepositoriesApi)
}

idea {
  setPackagePrefix("uk.org.lidalia.repositories.postgres")
}
