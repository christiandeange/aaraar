package sh.christian.aaraar

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.maps.shouldNotHaveKey
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.utils.animalJarPath
import sh.christian.aaraar.utils.deleteIfExists
import sh.christian.aaraar.utils.foo2JarPath
import sh.christian.aaraar.utils.fooJarPath
import sh.christian.aaraar.utils.withFile
import java.nio.file.Path
import kotlin.test.Test

class GenericJarArchiveTest {

  @Test
  fun `simple merge with classes`() {
    val animalClasses = animalJarPath.loadJar()
    val fooClasses = fooJarPath.loadJar()

    with(animalClasses + fooClasses) {
      this shouldHaveSize 4
      this shouldHaveKey "com/example/Animal.class"
      this shouldHaveKey "com/example/Cat.class"
      this shouldHaveKey "com/example/Dog.class"
      this shouldHaveKey "com/example/Foo.class"
    }
  }

  @Test
  fun `merge with self is redundant`() {
    val fooClasses1 = fooJarPath.loadJar()
    val fooClasses2 = fooJarPath.loadJar()

    with(fooClasses1 + fooClasses2) {
      this shouldHaveSize 1
      this shouldHaveKey "com/example/Foo.class"
    }
  }

  @Test
  fun `merge with classes with conflicting class files fails`() {
    val fooClasses = fooJarPath.loadJar()
    val foo2Classes = foo2JarPath.loadJar()

    shouldThrow<IllegalStateException> {
      fooClasses + foo2Classes
    }
  }

  @Test
  fun `shade with no rules does nothing`() {
    val shadedClasses = animalJarPath.loadJar().shaded()
    with(shadedClasses) {
      this shouldHaveSize 3
      this shouldHaveKey "com/example/Animal.class"
      this shouldHaveKey "com/example/Cat.class"
      this shouldHaveKey "com/example/Dog.class"
    }
  }

  @Test
  fun `shade by class name`() {
    val shadedClasses = animalJarPath.loadJar().shaded(
      classRenames = mapOf("com.example.Animal" to "com.example.Pet"),
    )
    with(shadedClasses) {
      this shouldHaveSize 3
      this shouldHaveKey "com/example/Pet.class"
      this shouldHaveKey "com/example/Cat.class"
      this shouldHaveKey "com/example/Dog.class"
    }
  }

  @Test
  fun `shade by package name`() {
    val shadedClasses = animalJarPath.loadJar().shaded(
      classRenames = mapOf("com.example.**" to "com.biganimalcorp.@1"),
    )
    with(shadedClasses) {
      this shouldHaveSize 3
      this shouldHaveKey "com/biganimalcorp/Animal.class"
      this shouldHaveKey "com/biganimalcorp/Cat.class"
      this shouldHaveKey "com/biganimalcorp/Dog.class"
    }
  }

  @Test
  fun `delete by class name`() {
    val shadedClasses = animalJarPath.loadJar().shaded(
      classDeletes = setOf("com.example.Cat"),
    )
    with(shadedClasses) {
      this shouldHaveSize 2
      this shouldHaveKey "com/example/Animal.class"
      this shouldHaveKey "com/example/Dog.class"
      this shouldNotHaveKey "com/example/Cat.class"
    }
  }

  @Test
  fun `delete all classes by package name`() {
    val shadedClasses = animalJarPath.loadJar().shaded(
      classDeletes = setOf("com.example.**"),
    )
    with(shadedClasses) {
      shouldBeEmpty()
    }
  }

  @Test
  fun `delete some classes by package name`() {
    val classpath = animalJarPath.loadJar() + fooJarPath.loadJar().shaded(
      classRenames = mapOf("com.example.**" to "com.foo.@1"),
    )

    with(classpath) {
      this shouldHaveSize 4
      this shouldHaveKey "com/example/Animal.class"
      this shouldHaveKey "com/example/Cat.class"
      this shouldHaveKey "com/example/Dog.class"
      this shouldHaveKey "com/foo/Foo.class"
    }

    val shadedClasspath = classpath.shaded(
      classDeletes = setOf("com.example.**"),
    )
    with(shadedClasspath) {
      this shouldHaveSize 1
      this shouldHaveKey "com/foo/Foo.class"
    }
  }

  @Test
  fun `delete by resource name`() {
    val shadedClasses = animalJarPath.loadJar().shaded(
      resourceDeletes = setOf("**/Cat.class"),
    )
    with(shadedClasses) {
      this shouldHaveSize 2
      this shouldHaveKey "com/example/Animal.class"
      this shouldHaveKey "com/example/Dog.class"
      this shouldNotHaveKey "com/example/Cat.class"
    }
  }

  @Test
  fun `can read from written jar`() {
    val originalJar = animalJarPath.loadJar()

    withFile {
      filePath.deleteIfExists()
      originalJar.writeTo(filePath)

      val rehydratedJar = filePath.loadJar()

      rehydratedJar.keys shouldContainExactly originalJar.keys
      rehydratedJar.keys.forEach { key ->
        withClue("Jar entry: $key") {
          rehydratedJar[key]!! shouldBe originalJar[key]!!
        }
      }
    }
  }

  private fun Path.loadJar(): GenericJarArchive {
    return GenericJarArchive.from(this, keepMetaFiles = true) ?: GenericJarArchive.NONE
  }

  private fun GenericJarArchive.shaded(
    classRenames: Map<String, String> = emptyMap(),
    classDeletes: Set<String> = emptySet(),
    resourceDeletes: Set<String> = emptySet(),
  ): GenericJarArchive {
    return shaded(ShadeConfiguration(classRenames, classDeletes, resourceDeletes))
  }
}
