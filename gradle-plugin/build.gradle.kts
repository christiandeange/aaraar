import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  @Suppress("DSL_SCOPE_VIOLATION") val plugins = libs.plugins

  alias(plugins.kotlin.jvm)
  `java-gradle-plugin`
  `kotlin-dsl`
  id("aaraar-publish")
}

group = "sh.christian.aaraar"
version = "0.0.2-SNAPSHOT"

dependencies {
  implementation(project(":core"))

  implementation(platform(kotlin("bom")))
  implementation(libs.agp.api)
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "11"
}

kotlin {
  sourceSets {
    all {
      languageSettings.apply {
        optIn("kotlin.RequiresOptIn")
      }
    }
  }

  jvmToolchain(11)
}

gradlePlugin {
  plugins {
    create("aaraar") {
      id = "sh.christian.aaraar"
      implementationClass = "sh.christian.aaraar.gradle.AarAarPlugin"
    }
  }
}

`aaraar-publish` {
  group.set("sh.christian.aaraar")
  artifact.set("gradle-plugin")
  version.set("0.0.2-SNAPSHOT")
}
