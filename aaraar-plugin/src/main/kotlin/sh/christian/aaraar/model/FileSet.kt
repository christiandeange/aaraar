package sh.christian.aaraar.model

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
    val duplicateKeys = mutableSetOf<String>()
    val mergedIndexedFiles = mutableMapOf<String, ByteArray>()
    (indexedFiles.entries + others.flatMap { it.indexedFiles.entries }).forEach { (key, value) ->
      if (mergedIndexedFiles.put(key, value) != null) {
        duplicateKeys.add(key)
      }
    }

    check(duplicateKeys.isEmpty()) {
      val filesToShow = duplicateKeys.toList().take(5)
      val moreFileCount = (duplicateKeys.count() - 5).coerceAtLeast(0)

      val toStringList = if (moreFileCount > 0) {
        filesToShow + "($moreFileCount more...)"
      } else {
        filesToShow
      }

      "Found differing files in file sets when merging: $toStringList"
    }

    return FileSet(mergedIndexedFiles)
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
