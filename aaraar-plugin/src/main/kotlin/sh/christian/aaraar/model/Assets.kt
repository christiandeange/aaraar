package sh.christian.aaraar.model

import java.nio.file.Path

class Assets
private constructor(
  private val files: FileSet,
) : Mergeable<Assets> {
  fun isEmpty(): Boolean = files.isEmpty()

  override operator fun plus(other: Assets): Assets {
    return Assets(files + other.files)
  }

  fun writeTo(path: Path) {
    files.writeTo(path)
  }

  companion object {
    fun from(path: Path): Assets {
      return FileSet.fromFileTree(path)
        ?.let { files -> Assets(files) }
        ?: Assets(files = FileSet.EMPTY)
    }
  }
}
