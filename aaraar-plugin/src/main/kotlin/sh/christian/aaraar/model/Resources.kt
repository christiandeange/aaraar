package sh.christian.aaraar.model

import java.nio.file.Path

class Resources
private constructor(
  private val files: CollapsedFileTree,
) {
  fun isEmpty(): Boolean = files.isEmpty()

  operator fun plus(other: Resources): Resources {
    return Resources(files + other.files)
  }

  fun writeTo(path: Path) {
    files.writeTo(path)
  }

  companion object {
    fun from(path: Path): Resources {
      return CollapsedFileTree.from(path)
        ?.let { files -> Resources(files) }
        ?: Resources(files = CollapsedFileTree.EMPTY)
    }
  }
}
