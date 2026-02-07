package sh.christian.aaraar.utils

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.properties.ReadOnlyProperty

private object ResourceLoader

private val resourceLoader = ResourceLoader::class.java.classLoader

val annotationsJarPath: Path by testFixtureJar()
val animalJarPath: Path by testFixtureJar()
val fooJarPath: Path by testFixtureJar()
val foo2JarPath: Path by testFixtureJar()
val ktLibraryJarPath: Path by testFixtureJar()
val serviceJarPath: Path by testFixtureJar()

val externalLibsPath: Path
  get() = Paths.get(resourceLoader.getResource("libs")!!.toURI())

private fun testFixtureJar(): ReadOnlyProperty<Any?, Path> {
  return ReadOnlyProperty { _, property ->
    Paths.get(resourceLoader.getResource("${property.name.removeSuffix("JarPath")}.jar")!!.toURI())
  }
}