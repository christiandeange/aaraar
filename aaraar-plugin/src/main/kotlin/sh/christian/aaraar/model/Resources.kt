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

  companion object {
    fun from(path: Path): Resources? {
      return CollapsedFileTree.from(path)?.let { files -> Resources(files) }
    }
  }
}
