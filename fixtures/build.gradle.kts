plugins {
  @Suppress("DSL_SCOPE_VIOLATION") val plugins = libs.plugins

  alias(plugins.kotlin.jvm)
  `kotlin-dsl`
  `java-test-fixtures`
  id("aaraar-detekt")
}

val agpVersion = project.findProperty("agpVersion")?.toString()
val toolsVersion = agpVersion
  ?.split(".")
  ?.mapIndexed { i, str -> if (i == 0) (str.toInt() + 23) else str }
  ?.joinToString(".")

dependencies {
  testFixturesApi(project(":core"))

  testFixturesImplementation(libs.kotest)
  testFixturesImplementation(libs.decompiler)
  testFixturesImplementation(libs.jimfs)

  if (toolsVersion.isNullOrBlank()) {
    testFixturesApi(libs.agp.layoutlib)
    testFixturesApi(libs.agp.tools.common)
    testFixturesApi(libs.agp.tools.manifestmerger)
    testFixturesApi(libs.agp.tools.sdk)
  } else {
    testFixturesApi("com.android.tools.layoutlib:layoutlib-api:$toolsVersion")
    testFixturesApi("com.android.tools:common:$toolsVersion")
    testFixturesApi("com.android.tools.build:manifest-merger:$toolsVersion")
    testFixturesApi("com.android.tools:sdk-common:$toolsVersion")
  }
}

tasks.withType<Copy> {
  duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

val fixtureJarsDir = layout.buildDirectory.dir("fixture-jars")
val fixtureJars by configurations.registering

registerSourceSet("animal")
registerSourceSet("annotations")
registerSourceSet("foo")
registerSourceSet("foo2")
registerSourceSet("ktLibrary")
registerSourceSet("service")

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