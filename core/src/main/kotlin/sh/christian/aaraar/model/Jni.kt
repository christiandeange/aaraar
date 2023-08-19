package sh.christian.aaraar.model

import java.nio.file.Path

class Jni
internal constructor(
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
