package sh.christian.aaraar.model

import java.nio.file.Path

class Assets
internal constructor(
  val files: FileSet,
) : Mergeable<Assets> {
  override fun plus(others: List<Assets>): Assets {
    return Assets(files + others.map { it.files })
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
