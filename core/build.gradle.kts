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
  api(libs.kotlin.metadata)

  implementation(platform(kotlin("bom")))
  implementation(libs.asm)
  implementation(libs.gson)
  implementation(libs.javassist)
  implementation(libs.kotlinxml)

  compileOnly(libs.agp.layoutlib)
  compileOnly(libs.agp.tools.common)
  compileOnly(libs.agp.tools.manifestmerger)
  compileOnly(libs.agp.tools.sdk)

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
