plugins {
  @Suppress("DSL_SCOPE_VIOLATION") val plugins = libs.plugins

  alias(plugins.kotlin.jvm)
  `kotlin-dsl`
  `java-test-fixtures`
  id("aaraar-detekt")
}

dependencies {
  testFixturesApi(project(":core"))
  testFixturesApi(libs.agp.tools.manifestmerger)

  testFixturesImplementation(libs.kotest)
  testFixturesImplementation(libs.decompiler)
  testFixturesImplementation(libs.jimfs)
}
