package sh.christian.aaraar.model

import java.nio.file.Path

class Classes
private constructor(
  private val archive: GenericJarArchive,
) {
  companion object {
    fun from(path: Path): Classes? {
      return GenericJarArchive.from(path)?.let { archive -> Classes(archive) }
    }
  }
}
