package sh.christian.aaraar.model

import sh.christian.aaraar.model.GenericJarArchive.MergeResult.Conflict
import sh.christian.aaraar.model.GenericJarArchive.MergeResult.MergedContents
import sh.christian.aaraar.model.GenericJarArchive.MergeResult.Skip
import sh.christian.aaraar.utils.createJar
import sh.christian.aaraar.utils.deleteIfExists
import sh.christian.aaraar.utils.div
import sh.christian.aaraar.utils.mkdirs
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarInputStream

class GenericJarArchive
private constructor(
  private val archiveEntries: Map<String, ByteArray>,
) : Mergeable<GenericJarArchive>, Map<String, ByteArray> by archiveEntries {

  override operator fun plus(others: List<GenericJarArchive>): GenericJarArchive {
    val duplicateKeysDifferentValues = mutableSetOf<String>()

    @OptIn(ExperimentalStdlibApi::class)
    val newEntries: Map<String, ByteArray> = buildMap {
      putAll(this@GenericJarArchive)

      others.flatMap { it.entries }.forEach { (name, contents) ->
        if (name in this) {
          when (val result = merge(name, this[name]!!, contents)) {
            is Conflict -> duplicateKeysDifferentValues += name
            is MergedContents -> put(name, result.contents)
            is Skip -> remove(name)
          }
        } else {
          put(name, contents)
        }
      }
    }

    check(duplicateKeysDifferentValues.isEmpty()) {
      "Found differing files in jar archives when merging: $duplicateKeysDifferentValues"
    }

    return GenericJarArchive(newEntries)
  }

  fun writeTo(path: Path) {
    val tempClassesJar = Files.createTempFile("classes", ".jar").deleteIfExists()

    tempClassesJar.createJar { classesJar ->
      entries.forEach { (entry, contents) ->
        Files.write((classesJar / entry).mkdirs(), contents)
      }
    }

    Files.move(tempClassesJar, path)
  }

  private fun merge(
    entry: String,
    contents1: ByteArray,
    contents2: ByteArray,
  ): MergeResult {
    return when {
      contents1.contentEquals(contents2) -> MergedContents(contents1)
      entry.substringAfterLast('/') == "module-info.class" -> Skip
      entry.substringAfterLast('.') == "pro" -> Skip
      entry.startsWith("META-INF/services/") -> {
        MergedContents(
          (contents1.decodeToString() + "\n" + contents2.decodeToString()).trim().toByteArray()
        )
      }

      else -> Conflict
    }
  }

  private sealed class MergeResult {
    object Skip : MergeResult()
    object Conflict : MergeResult()
    class MergedContents(val contents: ByteArray) : MergeResult()
  }

  companion object {
    val NONE = GenericJarArchive(archiveEntries = emptyMap())

    fun from(path: Path): GenericJarArchive? {
      if (!Files.isRegularFile(path)) return null

      val jarInputStream = JarInputStream(Files.newInputStream(path))
      return jarInputStream.use { stream ->
        val indexedEntries = generateSequence { stream.nextJarEntry }
          .filterNot { it.isDirectory }
          .map { it.name to stream.readAllBytes() }
          .toMap()

        GenericJarArchive(indexedEntries)
      }
    }

    fun from(entries: Map<String, ByteArray>): GenericJarArchive {
      return GenericJarArchive(entries.toMap())
    }
  }
}
