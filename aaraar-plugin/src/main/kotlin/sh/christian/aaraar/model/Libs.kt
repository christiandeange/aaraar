package sh.christian.aaraar.model

import java.nio.file.Path

class Libs
private constructor(
  private val files: CollapsedFileTree,
) {
  fun isEmpty(): Boolean = files.isEmpty()

  operator fun plus(other: Libs): Libs {
    return Libs(files + other.files)
  }

  companion object {
    fun from(path: Path): Libs {
      return CollapsedFileTree.from(path)
        ?.let { files -> Libs(files) }
        ?: Libs(files = CollapsedFileTree.EMPTY)
    }
  }
}
