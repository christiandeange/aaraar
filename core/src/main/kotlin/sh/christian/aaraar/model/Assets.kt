package sh.christian.aaraar.model

import java.nio.file.Path

/**
 * Represents the contents of the `assets/` folder.
 */
class Assets
internal constructor(
  val files: FileSet,
) {
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
