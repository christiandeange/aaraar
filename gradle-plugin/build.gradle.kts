plugins {
  @Suppress("DSL_SCOPE_VIOLATION") val plugins = libs.plugins

  alias(plugins.kotlin.jvm)
  `java-gradle-plugin`
  `kotlin-dsl`
  id("aaraar-detekt")
  id("aaraar-publish")
}

dependencies {
  implementation(project(":core"))

  implementation(platform(kotlin("bom")))
  implementation(libs.agp.api)
}

kotlinDslPluginOptions {
  jvmTarget.set("11")
}

}

gradlePlugin {
  plugins {
    create("aaraar") {
      id = "sh.christian.aaraar"
      implementationClass = "sh.christian.aaraar.gradle.AarAarPlugin"
    }
  }
}
