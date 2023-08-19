package sh.christian.aaraar.model

import java.nio.file.Path

class Classes
internal constructor(
  val archive: GenericJarArchive,
) {
  fun shaded(shadeConfiguration: ShadeConfiguration): Classes {
    return Classes(archive.shaded(shadeConfiguration))
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
