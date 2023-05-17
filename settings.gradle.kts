rootProject.name = "aaraar"

pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

include(":agp-compat:agp7")
include(":agp-compat:agp8")
include(":agp-compat:base")
include(":core")
include(":gradle-plugin")

includeBuild("build-logic")
