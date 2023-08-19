package sh.christian.aaraar.model

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.maps.shouldNotHaveKey
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.utils.animalJarPath
import sh.christian.aaraar.utils.deleteIfExists
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.shaded
import sh.christian.aaraar.utils.withFile
import kotlin.test.Test

class GenericJarArchiveTest {

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
}
