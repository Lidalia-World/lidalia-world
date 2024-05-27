plugins {
  id("lidalia.world.kotlin-library-conventions")
  alias(libs.plugins.ideaext)
}

dependencies {
  api(platform(libs.arrow.stack))
  api(libs.arrow.core)
}

idea {
  setPackagePrefix("uk.org.lidalia.repositories")
}
