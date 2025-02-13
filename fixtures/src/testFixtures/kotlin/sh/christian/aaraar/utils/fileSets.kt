package sh.christian.aaraar.utils

import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.FileSet

fun FileSet.forEntry(entry: String) = FileSetEntry(this, entry)

fun FileSet.shouldContainExactly(vararg entries: String) {
  entries.forEach { entry ->
    forEntry(entry).shouldExist()
  }
  shouldHaveSize(entries.size)
}

data class FileSetEntry(
  private val fileSet: FileSet,
  private val name: String,
) {
  fun shouldExist() {
    fileSet[name].shouldNotBeNull()
  }

  fun shouldNotExist() {
    fileSet[name]?.decodeToString().shouldBeNull()
  }

  infix fun shouldHaveFileContents(contents: String) {
    val file = fileSet[name]
    file.shouldNotBeNull()
    file.decodeToString().normalizeWhitespace() shouldBe contents.trimIndent()
  }
}
