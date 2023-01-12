package sh.christian.aaraar.model

import java.nio.file.Path

class Classes
private constructor(
  private val archive: GenericJarArchive,
) {
  operator fun plus(other: Classes): Classes {
    return Classes(archive + other.archive)
  }

  companion object {
    fun from(path: Path): Classes? {
      return GenericJarArchive.from(path)?.let { archive -> Classes(archive) }
    }
  }
}
