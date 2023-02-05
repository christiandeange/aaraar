package sh.christian.aaraar.model

import java.nio.file.Path

class Classes
internal constructor(
  private val archive: GenericJarArchive,
) : Mergeable<Classes> {
  override operator fun plus(others: List<Classes>): Classes {
    return Classes(archive + others.map { it.archive })
  }

  operator fun plus(libs: Libs): Classes {
    return Classes(archive + libs.jars())
  }

  fun shaded(
    classRenames: Map<String, String>,
    classDeletes: Set<String>,
  ): Classes {
    return Classes(archive.shaded(classRenames, classDeletes))
  }

  fun writeTo(path: Path) {
    archive.writeTo(path)
  }

  companion object {
    fun from(
      path: Path,
      keepMetaFiles: Boolean,
    ): Classes {
      return GenericJarArchive.from(path, keepMetaFiles)
        ?.let { archive -> Classes(archive) }
        ?: Classes(GenericJarArchive.NONE)
    }
  }
}
