package sh.christian.aaraar.model

import java.nio.file.Path

class Jni
private constructor(
  private val files: CollapsedFileTree,
) {
  fun isEmpty(): Boolean = files.isEmpty()

  companion object {
    fun from(path: Path): Jni? {
      return CollapsedFileTree.from(path)?.let { files -> Jni(files) }
    }
  }
}
