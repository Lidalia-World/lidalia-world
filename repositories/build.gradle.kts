plugins {
  id("lidalia.world.kotlin-library-conventions")
  alias(libs.plugins.ideaext)
}

dependencies {

}

idea {
  setPackagePrefix("uk.org.lidalia.repositories")
}
