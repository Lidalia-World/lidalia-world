plugins {
  id("buildlogic.kotlin-library-conventions")
}

dependencies {
  api(libs.kotest.assertions.core)
  api(libs.kotest.framework.api)
  implementation(libs.slf4j.jul.to.slf4j)
}
