package sh.christian.aaraar.shading.impl

import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.maps.shouldNotHaveKey
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.merger.MergeRules
import sh.christian.aaraar.merger.impl.GenericJarArchiveMerger
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.utils.animalJarPath
import sh.christian.aaraar.utils.fooJarPath
import sh.christian.aaraar.utils.ktLibraryJarPath
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.serviceJarPath
import sh.christian.aaraar.utils.shouldBeDecompiledTo
import kotlin.test.Test

class GenericJarArchiveShaderTest {

  private val shader = GenericJarArchiveShader()

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
    val originalClasses = ktLibraryJarPath.loadJar()
    originalClasses shouldHaveKey "META-INF/fixtures_ktLibrary.kotlin_module"

    val shadedClasses = originalClasses.shaded(resourceDeletes = setOf("**/*.kotlin_module"))
    shadedClasses shouldNotHaveKey "META-INF/fixtures_ktLibrary.kotlin_module"
  }

  @Test
  fun `cannot delete classes by resource name`() {
    val originalClasses = ktLibraryJarPath.loadJar()
    originalClasses shouldHaveKey "sh/christian/mylibrary/Foo.class"

    val shadedClasses = originalClasses.shaded(resourceDeletes = setOf("**/Foo.class"))
    shadedClasses shouldHaveKey "sh/christian/mylibrary/Foo.class"
  }

  @Test
  fun `delete some classes by package name`() {
    val merger = GenericJarArchiveMerger(MergeRules.None)
    val classpath = merger.merge(
      animalJarPath.loadJar(),
      fooJarPath.loadJar().shaded(
        classRenames = mapOf("com.example.**" to "com.foo.@1"),
      ),
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
  fun `shading updates class name`() {
    val originalClasses = animalJarPath.loadJar()
    with(originalClasses["com/example/Animal.class"]) {
      this.shouldNotBeNull()
      this shouldBeDecompiledTo """
          package com.example;

          public interface Animal {
          }
        """
    }

    val shadedClasses = originalClasses.shaded(
      classRenames = mapOf("com.example.Animal" to "com.example.Pet"),
    )
    with(shadedClasses["com/example/Pet.class"]) {
      this.shouldNotBeNull()
      this shouldBeDecompiledTo """
          package com.example;

          public interface Pet {
          }
        """
    }
  }

  @Test
  fun `shading updates class references from other classes`() {
    val originalClasses = animalJarPath.loadJar()
    with(originalClasses["com/example/Dog.class"]) {
      this.shouldNotBeNull()
      this shouldBeDecompiledTo """
          package com.example;
  
          public class Dog implements Animal {
          }
        """
    }

    val shadedClasses = originalClasses.shaded(
      classRenames = mapOf("com.example.Animal" to "com.example.Pet"),
    )
    with(shadedClasses["com/example/Dog.class"]) {
      this.shouldNotBeNull()
      this shouldBeDecompiledTo """
          package com.example;
  
          public class Dog implements Pet {
          }
        """
    }
  }

  @Test
  fun `shading updates class references from service loader files`() {
    val originalClasses = serviceJarPath.loadJar()
    with(originalClasses["META-INF/services/java.nio.file.spi.CustomService"]) {
      this.shouldNotBeNull()
      this.decodeToString() shouldBe """
        com.example.MyCustomService
        com.example.RealCustomService
      """.trimIndent()
    }

    val shadedClasses = originalClasses.shaded(
      classRenames = mapOf("com.example.MyCustomService" to "com.example.EmptyCustomService"),
    )
    with(shadedClasses["META-INF/services/java.nio.file.spi.CustomService"]) {
      this.shouldNotBeNull()
      this.decodeToString() shouldBe """
        com.example.EmptyCustomService
        com.example.RealCustomService
      """.trimIndent()
    }
  }

  private fun GenericJarArchive.shaded(
    classRenames: Map<String, String> = emptyMap(),
    classDeletes: Set<String> = emptySet(),
    resourceDeletes: Set<String> = emptySet(),
  ): GenericJarArchive {
    return shader.shade(this, ShadeConfiguration(classRenames, classDeletes, resourceDeletes))
  }
}
