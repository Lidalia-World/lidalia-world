plugins {
  id("buildlogic.kotlin-common-conventions")
}

tasks.withType<Test> {
  // mockk brings in bytebuddy which needs this flag
  jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
}
