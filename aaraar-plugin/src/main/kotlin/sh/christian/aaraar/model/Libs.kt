package sh.christian.aaraar.model

import java.nio.file.Path

class Libs
private constructor(
  private val files: FileSet,
) {
  fun isEmpty(): Boolean = files.isEmpty()

  operator fun plus(other: Libs): Libs {
    return Libs(files + other.files)
  }

  fun writeTo(path: Path) {
    files.writeTo(path)
  }

  companion object {
    fun from(path: Path): Libs {
      return FileSet.fromFileTree(path)
        ?.let { files -> Libs(files) }
        ?: Libs(files = FileSet.EMPTY)
    }
  }
}
