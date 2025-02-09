package sh.christian.aaraar.shading

import io.kotest.matchers.maps.shouldBeEmpty
import sh.christian.aaraar.merger.MergeRules
import sh.christian.aaraar.merger.impl.GenericJarArchiveMerger
import sh.christian.aaraar.utils.animalJarPath
import sh.christian.aaraar.utils.fooJarPath
import sh.christian.aaraar.utils.forEntry
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.shouldContainExactly
import kotlin.test.Test

class GenericJarArchiveClassShaderTest {
  @Test
  fun `shade with no rules does nothing`() {
    val shadedClasses = animalJarPath.loadJar().shaded()
    shadedClasses.shouldContainExactly(
      "com/example/Animal.class",
      "com/example/Cat.class",
      "com/example/Dog.class",
    )
  }

  @Test
  fun `shade by class name`() {
    val shadedClasses = animalJarPath.loadJar().shaded(
      classRenames = mapOf("com.example.Animal" to "com.example.Pet"),
    )
    shadedClasses.shouldContainExactly(
      "com/example/Pet.class",
      "com/example/Cat.class",
      "com/example/Dog.class",
    )
  }

  @Test
  fun `shade by package name`() {
    val shadedClasses = animalJarPath.loadJar().shaded(
      classRenames = mapOf("com.example.**" to "com.biganimalcorp.@1"),
    )
    shadedClasses.shouldContainExactly(
      "com/biganimalcorp/Animal.class",
      "com/biganimalcorp/Cat.class",
      "com/biganimalcorp/Dog.class",
    )
  }

  @Test
  fun `delete by class name`() {
    val shadedClasses = animalJarPath.loadJar().shaded(
      classDeletes = setOf("com.example.Cat"),
    )
    shadedClasses.shouldContainExactly(
      "com/example/Animal.class",
      "com/example/Dog.class",
    )
  }

  @Test
  fun `delete all classes by package name`() {
    val shadedClasses = animalJarPath.loadJar().shaded(
      classDeletes = setOf("com.example.**"),
    )
    shadedClasses.shouldBeEmpty()
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

    classpath.shouldContainExactly(
      "com/example/Animal.class",
      "com/example/Cat.class",
      "com/example/Dog.class",
      "com/foo/Foo.class",
    )

    val shadedClasspath = classpath.shaded(
      classDeletes = setOf("com.example.**"),
    )
    shadedClasspath.shouldContainExactly(
      "com/foo/Foo.class",
    )
  }

  @Test
  fun `shading updates class name`() {
    val originalClasses = animalJarPath.loadJar()
    originalClasses.forEntry("com/example/Animal.class") shouldBeDecompiledTo """
      package com.example;

      public interface Animal {
      }
    """

    val shadedClasses = originalClasses.shaded(
      classRenames = mapOf("com.example.Animal" to "com.example.Pet"),
    )
    shadedClasses.forEntry("com/example/Pet.class") shouldBeDecompiledTo """
      package com.example;

      public interface Pet {
      }
    """
  }

  @Test
  fun `shading updates class references from other classes`() {
    val originalClasses = animalJarPath.loadJar()
    originalClasses.forEntry("com/example/Dog.class") shouldBeDecompiledTo """
      package com.example;

      public class Dog implements Animal {
      }
    """

    val shadedClasses = originalClasses.shaded(
      classRenames = mapOf("com.example.Animal" to "com.example.Pet"),
    )

    shadedClasses.forEntry("com/example/Dog.class") shouldBeDecompiledTo """
      package com.example;

      public class Dog implements Pet {
      }
    """
  }
}
