package sh.christian.aaraar.model

import sh.christian.aaraar.model.MergeResult.Conflict
import sh.christian.aaraar.model.MergeResult.MergedContents
import sh.christian.aaraar.model.MergeResult.Skip
import sh.christian.aaraar.utils.div
import sh.christian.aaraar.utils.mkdirs
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

class FileSet
private constructor(
  private val indexedFiles: Map<String, ByteArray>,
) : Mergeable<FileSet>, Map<String, ByteArray> by indexedFiles {
  override operator fun plus(others: List<FileSet>): FileSet {
    val duplicateKeysDifferentValues = mutableSetOf<String>()

    @OptIn(ExperimentalStdlibApi::class)
    val newEntries: Map<String, ByteArray> = buildMap {
      putAll(this@FileSet)

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
      val filesToShow = duplicateKeysDifferentValues.toList().take(5)
      val moreFileCount = (duplicateKeysDifferentValues.count() - 5).coerceAtLeast(0)

      val toStringList = if (moreFileCount > 0) {
        filesToShow + "($moreFileCount more...)"
      } else {
        filesToShow
      }

      "Found differing files in file sets when merging: $toStringList"
    }

    return FileSet(newEntries)
  }

  private fun merge(
    entry: String,
    contents1: ByteArray,
    contents2: ByteArray,
  ): MergeResult {
    return when {
      entry.substringAfterLast('.') == "jar" -> {
        val archive1 = GenericJarArchive.from(contents1, keepMetaFiles = true)!!
        val archive2 = GenericJarArchive.from(contents2, keepMetaFiles = true)!!
        MergedContents((archive1 + archive2).bytes())
      }

      else -> Conflict
    }
  }

  fun writeTo(path: Path) {
    indexedFiles.forEach { (entry, bytes) ->
      val filePath = (path / entry).mkdirs()
      Files.write(filePath, bytes)
    }
  }

  companion object {
    val EMPTY = FileSet(indexedFiles = emptyMap())

    fun from(fileSet: Map<String, ByteArray>): FileSet {
      return FileSet(fileSet)
    }

    fun fromFileTree(path: Path): FileSet? {
      if (!Files.exists(path)) return null

      val indexedFiles = Files.walk(path)
        .asSequence()
        .filter(Files::isRegularFile)
        .map { path.relativize(it).toString() to Files.readAllBytes(it) }
        .toMap()

      return FileSet(indexedFiles)
    }
  }
}
