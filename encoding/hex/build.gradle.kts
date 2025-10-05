plugins {
  id("buildlogic.kotlin-library-conventions")
  alias(libs.plugins.ideaext)
}

dependencies {
  api(projects.lidaliaKotlinLang)
  api(projects.lidaliaEncodingCore)
  api(libs.arrow.core)
  testImplementation(testFixtures(projects.lidaliaEncodingCore))
  testImplementation(libs.kotest.assertions.shared)
  testImplementation(libs.kotest.assertions.arrow)
  testImplementation(libs.apache.commons.lang)
}

idea {
  setPackagePrefix("uk.org.lidalia.encoding.hex")
}
