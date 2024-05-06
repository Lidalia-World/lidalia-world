plugins {
  id("lidalia.world.kotlin-library-conventions")
  alias(libs.plugins.ideaext)
}

dependencies {

  api(libs.arrow.core)
  api(projects.lidaliaKotlinLang)

  testImplementation(libs.reflections)
  testImplementation(libs.kotlin.reflect)
  testImplementation(libs.kotest.framework.datatest)
  testImplementation(libs.kotest.assertions.core)
}

idea {
  setPackagePrefix("uk.org.lidalia.uri")
}
