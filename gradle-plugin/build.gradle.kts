import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  @Suppress("DSL_SCOPE_VIOLATION") val plugins = libs.plugins

  alias(plugins.kotlin.jvm)
  `java-gradle-plugin`
  `kotlin-dsl`
  id("aaraar-detekt")
  id("aaraar-publish")
}

val fixtureJars by configurations.registering

dependencies {
  implementation(project(":core"))
  implementation(project(":agp-compat:agp7"))
  implementation(project(":agp-compat:agp8"))

  implementation(platform(kotlin("bom")))
  compileOnly(libs.agp.api.latest)

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
    test {
      resources.srcDirs(fixtureJars)
    }
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
