plugins {
  @Suppress("DSL_SCOPE_VIOLATION") val plugins = libs.plugins

  alias(plugins.kotlin.jvm)
  `kotlin-dsl`
  `java-test-fixtures`
  id("aaraar-detekt")
}

dependencies {
  testFixturesApi(project(":core"))
  testFixturesApi(libs.agp.tools.manifestmerger)

  testFixturesImplementation(libs.kotest)
  testFixturesImplementation(libs.decompiler)
  testFixturesImplementation(libs.jimfs)
}

val fixtureJarsDir = layout.buildDirectory.dir("fixture-jars")
val fixtureJars by configurations.registering

registerSourceSet("animal")
registerSourceSet("annotations")
registerSourceSet("foo")
registerSourceSet("foo2")
registerSourceSet("ktLibrary")

fun registerSourceSet(name: String) {
  val newSourceSet = sourceSets.create(name) {
    java.srcDir("src/$name/java")
    kotlin.srcDir("src/$name/kotlin")
    resources.srcDir("src/$name/resources")
  }

  val newSourceSetJar = tasks.register<Jar>("${name}Jar") {
    from(newSourceSet.output)
    destinationDirectory.set(fixtureJarsDir)
    archiveFileName.set("$name.jar")
  }

  artifacts {
    add(fixtureJars.name, newSourceSetJar.flatMap { it.destinationDirectory }) {
      builtBy(newSourceSetJar)
    }
  }
}
