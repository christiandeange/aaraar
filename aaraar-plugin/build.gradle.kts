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
  implementation(platform(kotlin("bom")))
  implementation(libs.agp.api)
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

gradlePlugin {
  plugins {
    create("aaraar") {
      id = "sh.christian.aaraar"
      implementationClass = "sh.christian.aaraar.AarAarPlugin"
    }
  }
}
