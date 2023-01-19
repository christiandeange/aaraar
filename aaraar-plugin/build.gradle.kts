import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  @Suppress("DSL_SCOPE_VIOLATION") val plugins = libs.plugins

  alias(plugins.kotlin.jvm)
  `java-gradle-plugin`
  `kotlin-dsl`
  `maven-publish`
}

group = "sh.christian.aaraar"
version = "1.0-SNAPSHOT"

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
