package sh.christian.aaraar.model

import java.nio.file.Path

/**
 * Represents the contents of the `classes.jar` file, the main runtime sources for an [ArtifactArchive].
 */
class Classes
internal constructor(
  val archive: GenericJarArchive,
) {
  fun writeTo(path: Path) {
    archive.writeTo(path)
  }

  companion object {
    fun from(jarArchive: GenericJarArchive): Classes = Classes(jarArchive)

    fun from(
      path: Path,
      keepMetaFiles: Boolean,
    ): Classes {
      return from(GenericJarArchive.from(path, keepMetaFiles) ?: GenericJarArchive.NONE)
    }
  }
}
