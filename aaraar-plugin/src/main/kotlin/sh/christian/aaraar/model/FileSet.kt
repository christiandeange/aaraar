package sh.christian.aaraar.model

import sh.christian.aaraar.utils.mapToSet
import sh.christian.aaraar.utils.mkdirs
import java.nio.charset.Charset
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

class FileSet
private constructor(
  private val indexedFiles: Map<Path, ByteArray>,
) {
  val fileSystem: FileSystem
    get() = indexedFiles.keys.firstOrNull()?.fileSystem ?: FileSystems.getDefault()

  fun isEmpty(): Boolean = indexedFiles.isEmpty()

  operator fun plus(other: FileSet): FileSet {
    val duplicateKeys = indexedFiles.keys intersect other.indexedFiles.keys
    val duplicateKeysDifferentValues = duplicateKeys.filter {
      !indexedFiles[it].contentEquals(other.indexedFiles[it])
    }

    check(duplicateKeysDifferentValues.isEmpty()) {
      val filesToShow = duplicateKeysDifferentValues.toList().take(5)
        .map { it.toAbsolutePath().toString() }
      val moreFileCount = (duplicateKeysDifferentValues.count() - 5).coerceAtLeast(0)

      val toStringList = if (moreFileCount > 0) {
        filesToShow + "($moreFileCount more...)"
      } else {
        filesToShow
      }

      "Found differing files in file sets when merging: $toStringList"
    }

    return FileSet(indexedFiles + other.indexedFiles)
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
