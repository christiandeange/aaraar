package sh.christian.aaraar.model

import java.nio.file.Path

class Assets
private constructor(
  private val files: CollapsedFileTree,
) {
  fun isEmpty(): Boolean = files.isEmpty()

  companion object {
    fun from(path: Path): Assets? {
      return CollapsedFileTree.from(path)?.let { files -> Assets(files) }
    }
  }
}
