package sh.christian.aaraar.model

import sh.christian.aaraar.utils.mkdirs
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

class FileSet
private constructor(
  private val indexedFiles: Map<Path, ByteArray>,
) : Mergeable<FileSet> {
  val fileSystem: FileSystem
    get() = indexedFiles.keys.firstOrNull()?.fileSystem ?: FileSystems.getDefault()

  override operator fun plus(others: List<FileSet>): FileSet {
    val duplicateKeys = mutableSetOf<Path>()
    val mergedIndexedFiles = mutableMapOf<Path, ByteArray>()
    (indexedFiles.entries + others.flatMap { it.indexedFiles.entries }).forEach { (key, value) ->
      if (mergedIndexedFiles.put(key, value) != null) {
        duplicateKeys.add(key)
      }
    }

    check(duplicateKeys.isEmpty()) {
      val filesToShow = duplicateKeys.toList().take(5)
        .map { it.toAbsolutePath().toString() }
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
    indexedFiles.forEach { (subpath, bytes) ->
      Files.write(path.resolve(subpath.toString()).mkdirs(), bytes)
    }
  }

  companion object {
    val EMPTY = FileSet(indexedFiles = emptyMap())

    fun from(fileSet: Map<Path, ByteArray>): FileSet {
      return FileSet(fileSet)
    }

    fun fromFileTree(path: Path): FileSet? {
      if (!Files.exists(path)) return null

      val indexedFiles = Files.walk(path)
        .asSequence()
        .filter(Files::isRegularFile)
        .map { path.relativize(it) to Files.readAllBytes(it) }
        .toMap()

      return FileSet(indexedFiles)
    }
  }
}
