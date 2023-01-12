package sh.christian.aaraar.model

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

  companion object {
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
