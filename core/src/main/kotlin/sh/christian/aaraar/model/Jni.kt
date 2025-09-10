package sh.christian.aaraar.model

import java.nio.file.Path

/**
 * Represents the compiled native files in the `jni/` folder.
 */
data class Jni(
  val files: FileSet,
) {
  fun writeTo(path: Path) {
    files.writeTo(path)
  }

  companion object {
    fun from(path: Path): Jni {
      return FileSet.fromFileTree(path)
        ?.let { files -> Jni(files) }
        ?: Jni(files = FileSet.EMPTY)
    }
  }
}
