package sh.christian.aaraar.utils

import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.GenericJarArchive
import java.nio.file.Path

fun Path.loadJar(): GenericJarArchive {
  return GenericJarArchive.from(this, keepMetaFiles = true) ?: GenericJarArchive.NONE
}

infix fun GenericJarArchive.forEntry(entry: String) = JarEntry(this, entry)

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
  fun shouldExist() {
    jarArchive[name].shouldNotBeNull()
  }

  infix fun shouldHaveFileContents(contents: String) {
    val file = jarArchive[name]
    file.shouldNotBeNull()
    file.decodeToString().normalizeWhitespace() shouldBe contents.trimIndent()
  }

  infix fun shouldBeDecompiledTo(contents: String) {
    val file = jarArchive[name]
    file.shouldNotBeNull()
    file shouldBeDecompiledTo contents
  }
}
