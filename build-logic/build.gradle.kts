plugins {
  @Suppress("DSL_SCOPE_VIOLATION") val plugins = libs.plugins

  alias(plugins.kotlin.jvm)
  `java-gradle-plugin`
  `kotlin-dsl`
}

dependencies {
  implementation(libs.maven.publish)
}

gradlePlugin {
  plugins {
    create("build-logic") {
      id = "aaraar-publish"
      implementationClass = "sh.christian.plugin.publishing.PublishingPlugin"
    }
  }
}
