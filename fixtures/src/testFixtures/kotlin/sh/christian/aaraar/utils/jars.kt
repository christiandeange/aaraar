package sh.christian.aaraar.utils

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.metadata.jvm.JvmMetadataVersion
import kotlinx.metadata.jvm.KmModule
import kotlinx.metadata.jvm.KmPackageParts
import kotlinx.metadata.jvm.KotlinModuleMetadata
import kotlinx.metadata.jvm.UnstableMetadataApi
import sh.christian.aaraar.model.GenericJarArchive
import java.nio.file.Path

fun Path.loadJar(): GenericJarArchive {
  return GenericJarArchive.from(this, keepMetaFiles = true) ?: GenericJarArchive.NONE
}

fun GenericJarArchive.forEntry(entry: String): JarEntry = JarEntry(this, entry)

fun GenericJarArchive.shouldContainExactly(vararg entries: String) {
  entries.forEach { entry ->
    forEntry(entry).shouldExist()
  }
  shouldHaveSize(entries.size)
}

data class JarEntry(
  private val jarArchive: GenericJarArchive,
  private val name: String,
) {
  fun shouldExist() = withClue("Jar entry: $name") {
    jarArchive[name].shouldNotBeNull()
  }

  fun shouldNotExist() = withClue("Jar entry: $name") {
    jarArchive[name]?.decodeToString().shouldBeNull()
  }

  infix fun shouldHaveFileContents(contents: String) = withClue("Jar entry: $name") {
    val file = jarArchive[name]
    file.shouldNotBeNull()
    file.decodeToString().normalizeWhitespace() shouldBe contents.trimIndent()
  }

  infix fun shouldBeDecompiledTo(contents: String) = withClue("Jar entry: $name") {
    val file = jarArchive[name]
    file.shouldNotBeNull()
    file shouldBeDecompiledTo contents
  }

  @UnstableMetadataApi
  fun shouldHaveKotlinMetadata(
    version: JvmMetadataVersion,
    packageParts: Map<String, KmPackageParts>,
  ) {
    shouldHaveKotlinMetadata(
      KotlinModuleMetadata(
        kmModule = KmModule().apply {
          this.packageParts.putAll(packageParts)
        },
        version = version,
      )
    )
  }

  @UnstableMetadataApi
  infix fun shouldHaveKotlinMetadata(metadata: KotlinModuleMetadata) = withClue("Jar entry: $name") {
    val file = jarArchive[name]
    file.shouldNotBeNull()
    KotlinModuleMetadata.read(file) should { fileMetadata ->
      fileMetadata.version shouldBe metadata.version
      fileMetadata.kmModule should { fileModule ->
        val module = metadata.kmModule

        withClue("Module metadata package names should be equal") {
          fileModule.packageParts.keys shouldContainExactly module.packageParts.keys
        }
        fileModule.packageParts.forEach { (packageName, parts) ->
          withClue("Package $packageName file facades") {
            parts.fileFacades shouldContainExactly module.packageParts[packageName]!!.fileFacades
          }
          withClue("Package $packageName multi-file class parts") {
            parts.multiFileClassParts shouldContainExactly module.packageParts[packageName]!!.multiFileClassParts
          }
        }
      }
    }
  }
}
