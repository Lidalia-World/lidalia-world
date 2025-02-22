plugins {
  id("buildlogic.kotlin-library-conventions")
}

dependencies {
  api(platform(libs.arrow.stack))
  api(libs.arrow.core)
}
