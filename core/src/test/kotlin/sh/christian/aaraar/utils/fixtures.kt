package sh.christian.aaraar.utils

import java.nio.file.Path
import java.nio.file.Paths

private object ResourceLoader

private val resourceLoader = ResourceLoader::class.java.classLoader

val animalJarPath: Path = Paths.get(resourceLoader.getResource("animal.jar")!!.toURI())
val fooJarPath: Path = Paths.get(resourceLoader.getResource("foo.jar")!!.toURI())
val foo2JarPath: Path = Paths.get(resourceLoader.getResource("foo2.jar")!!.toURI())
val externalLibsPath: Path = Paths.get(resourceLoader.getResource("libs")!!.toURI())
