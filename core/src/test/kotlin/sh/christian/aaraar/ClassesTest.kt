package sh.christian.aaraar

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.maps.shouldNotHaveKey
import sh.christian.aaraar.model.Classes
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.GenericJarArchive.Companion.NONE
import sh.christian.aaraar.model.Libs
import sh.christian.aaraar.utils.deleteIfExists
import sh.christian.aaraar.utils.withFile
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.Test

class ClassesTest {

  private val animalJarPath = Paths.get(javaClass.classLoader.getResource("animal.jar")!!.toURI())
  private val fooJarPath = Paths.get(javaClass.classLoader.getResource("foo.jar")!!.toURI())
  private val foo2JarPath = Paths.get(javaClass.classLoader.getResource("foo2.jar")!!.toURI())
  private val externalLibsPath = Paths.get(javaClass.classLoader.getResource("libs")!!.toURI())

  @Test
  fun `simple merge with classes`() {
    val animalClasses = animalJarPath.loadClasses()
    val fooClasses = fooJarPath.loadClasses()

    withClasses(animalClasses + fooClasses) {
      this shouldHaveSize 4
      this shouldHaveKey "com/example/Animal.class"
      this shouldHaveKey "com/example/Cat.class"
      this shouldHaveKey "com/example/Dog.class"
      this shouldHaveKey "com/example/Foo.class"
    }
  }

  @Test
  fun `simple merge with libs`() {
    val animalClasses = animalJarPath.loadClasses()
    val externalLibs = Libs.from(externalLibsPath)

    withClasses(animalClasses + externalLibs) {
      this shouldHaveSize 5
      this shouldHaveKey "com/example/Animal.class"
      this shouldHaveKey "com/example/Cat.class"
      this shouldHaveKey "com/example/Dog.class"
      this shouldHaveKey "com/example/Foo.class"
      this shouldHaveKey "com/external/Library.class"
    }
  }

  @Test
  fun `merge with self is redundant`() {
    val fooClasses1 = fooJarPath.loadClasses()
    val fooClasses2 = fooJarPath.loadClasses()

    withClasses(fooClasses1 + fooClasses2) {
      this shouldHaveSize 1
      this shouldHaveKey "com/example/Foo.class"
    }
  }

  @Test
  fun `merge with classes with conflicting class files fails`() {
    val fooClasses = fooJarPath.loadClasses()
    val foo2Classes = foo2JarPath.loadClasses()

    shouldThrow<IllegalStateException> {
      fooClasses + foo2Classes
    }
  }

  @Test
  fun `merge with libs with conflicting class files fails`() {
    val foo2Classes = foo2JarPath.loadClasses()
    val fooLibs = Libs.from(externalLibsPath)

    shouldThrow<IllegalStateException> {
      foo2Classes + fooLibs
    }
  }

  @Test
  fun `shade by class name`() {
    val shadedClasses = animalJarPath.loadClasses().shaded(
      classRenames = mapOf("com.example.Animal" to "com.example.Pet"),
      classDeletes = emptySet(),
    )
    withClasses(shadedClasses) {
      this shouldHaveSize 3
      this shouldHaveKey "com/example/Pet.class"
      this shouldHaveKey "com/example/Cat.class"
      this shouldHaveKey "com/example/Dog.class"
    }
  }

  @Test
  fun `shade by package name`() {
    val shadedClasses = animalJarPath.loadClasses().shaded(
      classRenames = mapOf("com.example.**" to "com.biganimalcorp.@1"),
      classDeletes = emptySet(),
    )
    withClasses(shadedClasses) {
      this shouldHaveSize 3
      this shouldHaveKey "com/biganimalcorp/Animal.class"
      this shouldHaveKey "com/biganimalcorp/Cat.class"
      this shouldHaveKey "com/biganimalcorp/Dog.class"
    }
  }

  @Test
  fun `delete by class name`() {
    val shadedClasses = animalJarPath.loadClasses().shaded(
      classRenames = emptyMap(),
      classDeletes = setOf("com.example.Cat"),
    )
    withClasses(shadedClasses) {
      this shouldHaveSize 2
      this shouldHaveKey "com/example/Animal.class"
      this shouldHaveKey "com/example/Dog.class"
      this shouldNotHaveKey "com/example/Cat.class"
    }
  }

  @Test
  fun `delete all classes by package name`() {
    val shadedClasses = animalJarPath.loadClasses().shaded(
      classRenames = emptyMap(),
      classDeletes = setOf("com.example.**"),
    )
    withClasses(shadedClasses) {
      shouldBeEmpty()
    }
  }

  @Test
  fun `delete some classes by package name`() {
    val classpath = animalJarPath.loadClasses() + fooJarPath.loadClasses().shaded(
      classRenames = mapOf("com.example.**" to "com.foo.@1"),
      classDeletes = emptySet(),
    )

    withClasses(classpath) {
      this shouldHaveSize 4
      this shouldHaveKey "com/example/Animal.class"
      this shouldHaveKey "com/example/Cat.class"
      this shouldHaveKey "com/example/Dog.class"
      this shouldHaveKey "com/foo/Foo.class"
    }

    val shadedClasspath = classpath.shaded(
      classRenames = emptyMap(),
      classDeletes = setOf("com.example.**"),
    )
    withClasses(shadedClasspath) {
      this shouldHaveSize 1
      this shouldHaveKey "com/foo/Foo.class"
    }
  }

  private fun withClasses(
    classes: Classes,
    block: GenericJarArchive.() -> Unit,
  ) {
    withFile {
      filePath.deleteIfExists()
      classes.writeTo(filePath)

      with(GenericJarArchive.from(filePath, true) ?: NONE, block)
    }
  }

  private fun Path.loadClasses(): Classes {
    return Classes(GenericJarArchive.from(this, keepMetaFiles = true)!!)
  }
}
