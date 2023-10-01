plugins {
  id("lidalia.world.kotlin-library-conventions")
}

dependencies {
  testImplementation(libs.reflections)
  testImplementation(libs.kotlin.reflect)
  testImplementation(libs.kotest.framework.datatest)
}
