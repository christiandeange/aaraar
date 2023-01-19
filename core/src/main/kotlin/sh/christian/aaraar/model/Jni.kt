package sh.christian.aaraar.model

import java.nio.file.Path

class Jni
private constructor(
  private val files: FileSet,
) : Mergeable<Jni> {
  override fun plus(others: List<Jni>): Jni {
    return Jni(files + others.map { it.files })
  }

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
