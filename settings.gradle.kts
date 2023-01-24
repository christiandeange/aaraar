rootProject.name = "aaraar"

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
  }
}

include(":core")
include(":gradle-plugin")

includeBuild("build-logic")
