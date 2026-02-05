plugins {
  val libs = libs
  id("org.jetbrains.kotlin.jvm") version libs.versions.kotlin.get()
  id("org.jetbrains.dokka") version libs.versions.dokka.get()
}

dokka {
  dokkaPublications.html {
    outputDirectory.set(file("$rootDir/docs/kdoc"))
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}
