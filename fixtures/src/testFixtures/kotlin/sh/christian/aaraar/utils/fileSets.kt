package sh.christian.aaraar.utils

import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.FileSet

infix fun FileSet.forEntry(entry: String) = FileSetEntry(this, entry)

fun FileSet.shouldContainExactly(vararg entries: String) {
  entries.forEach { entry ->
    forEntry(entry).shouldExist()
  }
  shouldHaveSize(entries.size)
}

data class FileSetEntry(
  private val jarArchive: FileSet,
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
}
