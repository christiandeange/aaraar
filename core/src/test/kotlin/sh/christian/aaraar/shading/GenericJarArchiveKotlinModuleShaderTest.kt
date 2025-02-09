package sh.christian.aaraar.shading

import kotlinx.metadata.jvm.JvmMetadataVersion
import kotlinx.metadata.jvm.KmPackageParts
import kotlinx.metadata.jvm.UnstableMetadataApi
import sh.christian.aaraar.utils.forEntry
import sh.christian.aaraar.utils.ktLibraryJarPath
import sh.christian.aaraar.utils.loadJar
import kotlin.test.Test

@OptIn(UnstableMetadataApi::class)
class GenericJarArchiveKotlinModuleShaderTest {
  @Test
  fun `default kotlin_module file without shading`() {
    ktLibraryJarPath.loadJar()
      .forEntry("META-INF/fixtures_ktLibrary.kotlin_module")
      .shouldHaveKotlinMetadata(
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

  @Test
  fun `shading updates class references in kotlin_module file`() {
    val shadedClasses = ktLibraryJarPath.loadJar().shaded(
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

  @Test
  fun `deleting some class references removes them in kotlin_module file`() {
    val shadedClasses = ktLibraryJarPath.loadJar().shaded(
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

  @Test
  fun `deleting all class references removes the kotlin_module file`() {
    val shadedClasses = ktLibraryJarPath.loadJar().shaded(
      classDeletes = setOf(
        "sh.christian.mylibrary.Foos",
        "sh.christian.mylibrary.FooInternals",
      ),
    )
    shadedClasses.forEntry("META-INF/fixtures_ktLibrary.kotlin_module").shouldNotExist()
  }
}
