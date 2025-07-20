@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  @Suppress("DSL_SCOPE_VIOLATION") val plugins = libs.plugins

  alias(plugins.kotlin.jvm)
  `kotlin-dsl`
  id("aaraar-detekt")
  id("aaraar-publish")
}

val fixtureJars by configurations.registering

dependencies {
  api(libs.agp.tools.common)
  api(libs.kotlin.metadata)

  implementation(platform(kotlin("bom")))
  implementation(libs.agp.layoutlib)
  implementation(libs.agp.tools.manifestmerger)
  implementation(libs.agp.tools.sdk)
  implementation(libs.asm)
  implementation(libs.gson)
  implementation(libs.javassist)
  implementation(libs.kotlinxml)

  testImplementation(testFixtures(project(":fixtures")))
  testImplementation(kotlin("test"))
  testImplementation(libs.kotest)

  fixtureJars(project(":fixtures", configuration = "fixtureJars"))
}

tasks.test {
  useJUnitPlatform()

  testLogging {
    exceptionFormat = TestExceptionFormat.FULL
    events = TestLogEvent.values().toSet() - TestLogEvent.STARTED
  }
}

kotlin {
  sourceSets {
    all {
      languageSettings.apply {
        optIn("kotlin.ExperimentalStdlibApi")
      }
    }

    test {
      resources.srcDirs(fixtureJars)
    }
  }
}
