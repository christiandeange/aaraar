rootProject.name = "aaraar"

pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven("https://www.jetbrains.com/intellij-repository/releases/")
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven("https://www.jetbrains.com/intellij-repository/releases/")
  }
}

include(":agp-compat:agp7")
include(":agp-compat:agp8")
include(":agp-compat:agp9")
include(":agp-compat:base")
include(":core")
include(":fixtures")
include(":gradle-plugin")

includeBuild("build-logic")
