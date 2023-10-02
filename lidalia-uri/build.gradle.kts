plugins {
  id("lidalia.world.kotlin-library-conventions")
}

dependencies {

  implementation(projects.lidaliaKotlinLang)

  testImplementation(libs.reflections)
  testImplementation(libs.kotlin.reflect)
  testImplementation(libs.kotest.framework.datatest)
  testImplementation(libs.kotest.assertions.api)
  testImplementation(libs.kotest.assertions.core)
}
