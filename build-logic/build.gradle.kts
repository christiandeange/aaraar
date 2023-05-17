plugins {
  @Suppress("DSL_SCOPE_VIOLATION") val plugins = libs.plugins

  alias(plugins.kotlin.jvm)
  `java-gradle-plugin`
  `kotlin-dsl`
}

dependencies {
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

  implementation(libs.detekt.plugin)
  implementation(libs.maven.publish)
}

kotlinDslPluginOptions {
  jvmTarget.set("11")
}

tasks.withType<JavaCompile>().configureEach {
  sourceCompatibility = "11"
  targetCompatibility = "11"
}

gradlePlugin {
  plugins {
    create("aaraar-publish") {
      id = "aaraar-publish"
      implementationClass = "sh.christian.plugin.PublishingPlugin"
    }

    create("aaraar-detekt") {
      id = "aaraar-detekt"
      implementationClass = "sh.christian.plugin.DetektPlugin"
    }
  }
}
