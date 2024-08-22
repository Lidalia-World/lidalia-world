plugins {
  id("lidalia.world.kotlin-library-conventions")
  alias(libs.plugins.ideaext)
}

dependencies {
  api(platform(libs.arrow.stack))
  api(libs.arrow.core)
  api(project(":lidalia-repositories-api"))
}

idea {
  setPackagePrefix("uk.org.lidalia.repositories.postgres")
}
