package sh.christian.aaraar.utils

import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.AndroidManifest
import sh.christian.aaraar.model.classeditor.ClassReference

infix fun AndroidManifest.shouldBe(contents: String) {
  val output = toString().normalizeWhitespace()
  output shouldBe contents.trimIndent()
}

infix fun ClassReference.shouldBeDecompiledTo(contents: String) {
  val output = decompile(this).normalizeWhitespace()
  output shouldBe contents.trimIndent()
}

private fun String.normalizeWhitespace() = trim().replace("\t", "    ").replace("\r\n", "\n")
