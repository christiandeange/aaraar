package sh.christian.aaraar.shading.impl

import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveKey
import io.kotest.matchers.maps.shouldNotHaveKey
import kotlinx.metadata.jvm.JvmMetadataVersion
import kotlinx.metadata.jvm.KmPackageParts
import kotlinx.metadata.jvm.UnstableMetadataApi
import sh.christian.aaraar.merger.MergeRules
import sh.christian.aaraar.merger.impl.GenericJarArchiveMerger
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.utils.animalJarPath
import sh.christian.aaraar.utils.fooJarPath
import sh.christian.aaraar.utils.forEntry
import sh.christian.aaraar.utils.ktLibraryJarPath
import sh.christian.aaraar.utils.loadJar
import sh.christian.aaraar.utils.serviceJarPath
import sh.christian.aaraar.utils.shouldContainExactly
import kotlin.test.Test

class GenericJarArchiveShaderTest {

  private val shader = GenericJarArchiveShader()

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

  @Test
  fun `shading updates class references from service loader files`() {
    val originalClasses = serviceJarPath.loadJar()
    originalClasses.forEntry("META-INF/services/java.nio.file.spi.CustomService") shouldHaveFileContents """
      com.example.MyCustomService
      com.example.RealCustomService
    """

    val shadedClasses = originalClasses.shaded(
      classRenames = mapOf("com.example.MyCustomService" to "com.example.EmptyCustomService"),
    )
    shadedClasses.forEntry("META-INF/services/java.nio.file.spi.CustomService") shouldHaveFileContents """
      com.example.EmptyCustomService
      com.example.RealCustomService
    """
  }

  @OptIn(UnstableMetadataApi::class)
  @Test
  fun `shading updates class references in kotlin_module file`() {
    val originalClasses = ktLibraryJarPath.loadJar()
    originalClasses.forEntry("META-INF/fixtures_ktLibrary.kotlin_module").let {
      it.shouldExist()
      it.shouldHaveKotlinMetadata(
        version = JvmMetadataVersion(1, 8, 0),
        packageParts = mapOf(
          "sh.christian.mylibrary" to KmPackageParts(
            fileFacades = mutableListOf(
              "sh/christian/mylibrary/FooInternals",
              "sh/christian/mylibrary/Foos",
            ),
            multiFileClassParts = mutableMapOf(),
          ),
        ),
      )
    }

    val shadedClasses = originalClasses.shaded(
      classRenames = mapOf("sh.christian.mylibrary.**" to "sh.christian.foolib.@1"),
    )
    shadedClasses.forEntry("META-INF/fixtures_ktLibrary.kotlin_module").let {
      it.shouldExist()
      it.shouldHaveKotlinMetadata(
        version = JvmMetadataVersion(1, 8, 0),
        packageParts = mapOf(
          "sh.christian.foolib" to KmPackageParts(
            fileFacades = mutableListOf(
              "sh/christian/foolib/FooInternals",
              "sh/christian/foolib/Foos",
            ),
            multiFileClassParts = mutableMapOf(),
          ),
        ),
      )
    }
  }

  @OptIn(UnstableMetadataApi::class)
  @Test
  fun `deleting some class references removes them in kotlin_module file`() {
    val originalClasses = ktLibraryJarPath.loadJar()
    originalClasses.forEntry("META-INF/fixtures_ktLibrary.kotlin_module").let {
      it.shouldExist()
      it.shouldHaveKotlinMetadata(
        version = JvmMetadataVersion(1, 8, 0),
        packageParts = mapOf(
          "sh.christian.mylibrary" to KmPackageParts(
            fileFacades = mutableListOf(
              "sh/christian/mylibrary/FooInternals",
              "sh/christian/mylibrary/Foos",
            ),
            multiFileClassParts = mutableMapOf(),
          ),
        ),
      )
    }

    val shadedClasses = originalClasses.shaded(
      classDeletes = setOf("sh.christian.mylibrary.Foos"),
    )
    shadedClasses.forEntry("META-INF/fixtures_ktLibrary.kotlin_module").let {
      it.shouldExist()
      it.shouldHaveKotlinMetadata(
        version = JvmMetadataVersion(1, 8, 0),
        packageParts = mapOf(
          "sh.christian.mylibrary" to KmPackageParts(
            fileFacades = mutableListOf(
              "sh/christian/mylibrary/FooInternals",
            ),
            multiFileClassParts = mutableMapOf(),
          ),
        ),
      )
    }
  }

  @OptIn(UnstableMetadataApi::class)
  @Test
  fun `deleting all class references removes the kotlin_module file`() {
    val originalClasses = ktLibraryJarPath.loadJar()
    originalClasses.forEntry("META-INF/fixtures_ktLibrary.kotlin_module").let {
      it.shouldExist()
      it.shouldHaveKotlinMetadata(
        version = JvmMetadataVersion(1, 8, 0),
        packageParts = mapOf(
          "sh.christian.mylibrary" to KmPackageParts(
            fileFacades = mutableListOf(
              "sh/christian/mylibrary/FooInternals",
              "sh/christian/mylibrary/Foos",
            ),
            multiFileClassParts = mutableMapOf(),
          ),
        ),
      )
    }

    val shadedClasses = originalClasses.shaded(
      classDeletes = setOf(
        "sh.christian.mylibrary.Foos",
        "sh.christian.mylibrary.FooInternals",
      ),
    )
    shadedClasses.forEntry("META-INF/fixtures_ktLibrary.kotlin_module").shouldNotExist()
  }

  private fun GenericJarArchive.shaded(
    classRenames: Map<String, String> = emptyMap(),
    classDeletes: Set<String> = emptySet(),
    resourceDeletes: Set<String> = emptySet(),
  ): GenericJarArchive {
    return shader.shade(this, ShadeConfiguration(classRenames, classDeletes, resourceDeletes))
  }
}
