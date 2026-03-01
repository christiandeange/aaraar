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

val libTinySource = layout.projectDirectory.file("src/nativelib/c/tiny.c")
val libTinyLibrary = layout.buildDirectory.file("nativelib/libtiny.so").get()

val compileLibTiny = tasks.register<Exec>("buildNativelib") {
  description = "Build the native test fixture library (requires zig: https://ziglang.org/download/)"
  group = "build"

  inputs.file(libTinySource)
  outputs.file(libTinyLibrary)

  doFirst {
    libTinyLibrary.asFile.parentFile.mkdirs()
  }

  commandLine(
    "zig", "cc",
    "-O3",
    "-flto",
    "-Wl",
    "-target", "x86_64-linux-gnu",
    "-shared",
    "-fPIC",
    "-o", libTinyLibrary.asFile.absolutePath,
    libTinySource.asFile.absolutePath,
  )
}

val fixtureJarsDir = layout.buildDirectory.dir("fixture-jars")
val fixtureJars by configurations.registering

registerSourceSet("animal")
registerSourceSet("annotations")
registerSourceSet("foo")
registerSourceSet("foo2")
registerSourceSet("ktLibrary")
registerSourceSet("nativelib") { sourceSet ->
  sourceSet.resources.srcDir(compileLibTiny.map { libTinyLibrary.asFile.parentFile })
}
registerSourceSet("service")

fun registerSourceSet(
  name: String,
  configure: (SourceSet) -> Unit = { },
) {
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

  configure(newSourceSet)
}
