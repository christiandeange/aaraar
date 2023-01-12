package sh.christian.aaraar.model

import sh.christian.aaraar.utils.mkdirs
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

class CollapsedFileTree(
  val indexedFiles: Map<Path, ByteArray>,
) {
  fun isEmpty(): Boolean = indexedFiles.isEmpty()

  operator fun plus(other: CollapsedFileTree): CollapsedFileTree {
    return CollapsedFileTree(indexedFiles + other.indexedFiles)
  }

  fun writeTo(path: Path) {
    indexedFiles.forEach { (subpath, bytes) ->
      Files.write(path.resolve(subpath).mkdirs(), bytes)
    }
  }

  companion object {
    val EMPTY = CollapsedFileTree(indexedFiles = emptyMap())

    fun from(path: Path): CollapsedFileTree? {
      if (!Files.exists(path)) return null

      val indexedFiles = Files.walk(path)
        .asSequence()
        .filter(Files::isRegularFile)
        .map { path.relativize(it) to Files.readAllBytes(it) }
        .toMap()

      return CollapsedFileTree(indexedFiles)
    }
  }
}
