import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  @Suppress("DSL_SCOPE_VIOLATION") val plugins = libs.plugins

  alias(plugins.kotlin.jvm)
  `kotlin-dsl`
  id("aaraar-detekt")
  id("aaraar-publish")
}

dependencies {
  api(libs.agp.tools.common)

  implementation(platform(kotlin("bom")))
  implementation(libs.agp.layoutlib)
  implementation(libs.agp.tools.manifestmerger)
  implementation(libs.agp.tools.sdk)
  implementation(libs.asm)
  implementation(libs.gson)
  implementation(libs.jarjar) {
    exclude(
      group = libs.asm.get().module.group,
      module = libs.asm.get().module.name,
    )
  }
  implementation(libs.javassist)
  implementation(libs.kotlinxml)

  testImplementation(kotlin("test"))
  testImplementation(libs.decompiler)
  testImplementation(libs.jimfs)
  testImplementation(libs.kotest)
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
  }
}
