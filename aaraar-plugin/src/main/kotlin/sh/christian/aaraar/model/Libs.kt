package sh.christian.aaraar.model

import java.nio.file.Path

class Libs
private constructor(
  private val files: CollapsedFileTree,
) {
  fun isEmpty(): Boolean = files.isEmpty()

  companion object {
    fun from(path: Path): Libs? {
      return CollapsedFileTree.from(path)?.let { files -> Libs(files) }
    }
  }
}
