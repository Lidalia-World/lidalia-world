plugins {
  id("lidalia.world.kotlin-library-conventions")
  alias(libs.plugins.ideaext)
}

dependencies {
  api(platform(libs.arrow.stack))
  api(libs.arrow.core)
  api(project(":lidalia-repositories-api"))
  api(project(":lidalia-repositories-postgres"))
  api(project(":lidalia-uri"))
}

idea {
  setPackagePrefix("uk.org.lidalia.example")
}
