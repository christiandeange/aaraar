package sh.christian.aaraar.model

import java.nio.file.Path

class Assets
private constructor(
  private val files: CollapsedFileTree,
) {
  fun isEmpty(): Boolean = files.isEmpty()

  operator fun plus(other: Assets): Assets {
    return Assets(files + other.files)
  }

  fun writeTo(path: Path) {
    files.writeTo(path)
  }

  companion object {
    fun from(path: Path): Assets {
      return CollapsedFileTree.from(path)
        ?.let { files -> Assets(files) }
        ?: Assets(files = CollapsedFileTree.EMPTY)
    }
  }
}
