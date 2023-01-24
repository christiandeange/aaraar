import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  @Suppress("DSL_SCOPE_VIOLATION") val plugins = libs.plugins

  alias(plugins.kotlin.jvm)
  `kotlin-dsl`
  id("aaraar-publish")
}

group = "sh.christian.aaraar"
version = "0.0.2-SNAPSHOT"

dependencies {
  implementation(platform(kotlin("bom")))
  implementation(libs.agp.layoutlib)
  implementation(libs.agp.tools.common)
  implementation(libs.agp.tools.manifestmerger)
  implementation(libs.agp.tools.sdk)
  implementation(libs.asm)
  implementation(libs.jarjar) {
    exclude(
      group = libs.asm.get().module.group,
      module = libs.asm.get().module.name,
    )
  }
  implementation(libs.kotlinxml)

  testImplementation(kotlin("test"))
  testImplementation(libs.jimfs)
}

tasks.test {
  useJUnitPlatform()
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

`aaraar-publish` {
  group.set("sh.christian.aaraar")
  artifact.set("core")
  version.set("0.0.2-SNAPSHOT")
}
