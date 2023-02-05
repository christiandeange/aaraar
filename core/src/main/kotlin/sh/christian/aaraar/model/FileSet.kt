package sh.christian.aaraar.model

import sh.christian.aaraar.utils.div
import sh.christian.aaraar.utils.mkdirs
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

class FileSet
internal constructor(
  private val indexedFiles: Map<String, ByteArray>,
) : Mergeable<FileSet>, Map<String, ByteArray> by indexedFiles {
  override operator fun plus(others: List<FileSet>): FileSet {
    return FileSet(mergeContents(this, others))
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
