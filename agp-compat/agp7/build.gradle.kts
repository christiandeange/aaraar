plugins {
  @Suppress("DSL_SCOPE_VIOLATION") val plugins = libs.plugins

  alias(plugins.kotlin.jvm)
  `kotlin-dsl`
  id("aaraar-detekt")
  id("aaraar-publish")
}

dependencies {
  api(project(":agp-compat:base"))
  compileOnly(libs.agp.api7)
}

kotlinDslPluginOptions {
  jvmTarget.set("11")
}

tasks.withType<JavaCompile>().configureEach {
  sourceCompatibility = "11"
  targetCompatibility = "11"
}
