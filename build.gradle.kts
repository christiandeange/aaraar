import org.jetbrains.dokka.gradle.DokkaCollectorTask
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask

plugins {
  val libs = libs
  id("org.jetbrains.kotlin.jvm") version libs.versions.kotlin.get()
  id("org.jetbrains.dokka") version libs.versions.dokka.get()
}

tasks.withType<DokkaMultiModuleTask>().configureEach {
  outputDirectory.set(file("$rootDir/docs/kdoc"))
}

tasks.withType<DokkaCollectorTask>().configureEach {
  outputDirectory.set(file("$rootDir/docs/kdoc"))
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}
