plugins {
  alias(libs.plugins.kotlin.jvm)
  `kotlin-dsl-base`
  id("aaraar-detekt")
  id("aaraar-publish")
}

dependencies {
  api(project(":agp-compat:base"))
  compileOnly(libs.agp.api7)
}
