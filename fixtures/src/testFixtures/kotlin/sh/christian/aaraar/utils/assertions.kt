package sh.christian.aaraar.utils

import io.kotest.matchers.shouldBe
import sh.christian.aaraar.model.AndroidManifest
import sh.christian.aaraar.model.classeditor.ClassReference
import java.nio.file.Files
import java.nio.file.Path

infix fun Path.shouldHaveContents(contents: String) {
  val output = Files.readString(this).normalizeWhitespace()
  output shouldBe contents.trimIndent()
}

infix fun AndroidManifest.shouldBe(contents: String) {
  val output = toString().normalizeWhitespace()
  output shouldBe contents.trimIndent()
}

infix fun ClassReference.shouldBeDecompiledTo(contents: String) {
  val output = decompile(this).normalizeWhitespace()
  output shouldBe contents.trimIndent()
}

infix fun ByteArray.shouldBeDecompiledTo(contents: String) {
  val output = decompile(this).normalizeWhitespace()
  output shouldBe contents.trimIndent()
}

fun String.normalizeWhitespace() = trim().replace("\t", "    ").replace("\r\n", "\n")
