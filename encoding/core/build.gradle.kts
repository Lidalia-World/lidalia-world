plugins {
  id("buildlogic.kotlin-library-conventions")
  alias(libs.plugins.ideaext)
  `java-test-fixtures`
}

dependencies {
  api(projects.lidaliaKotlinLang)
  api(libs.arrow.core)

  testFixturesImplementation(platform(libs.kotest.bom))
//  testFixturesImplementation(projects.lidaliaRepositoriesInMemory)
  testFixturesImplementation(libs.kotest.runner.junit5)
  testFixturesImplementation(libs.kotest.assertions.core)
  testFixturesImplementation(libs.kotest.assertions.arrow)

}

idea {
  setPackagePrefix("uk.org.lidalia.encoding.core")
}
