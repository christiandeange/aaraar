plugins {
  alias(libs.plugins.kotlin.jvm)
  `kotlin-dsl-base`
  id("aaraar-detekt")
  id("aaraar-publish")
}

dependencies {
  api(platform(libs.kotlin.bom))
}
