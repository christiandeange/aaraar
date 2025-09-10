package sh.christian.aaraar.model

import java.nio.file.Path

/**
 * Represents the contents of the `classes.jar` file, the main runtime sources for an [ArtifactArchive].
 */
class Classes(
  val archive: GenericJarArchive,
) {
  fun writeTo(path: Path) {
    archive.writeTo(path)
  }

  companion object {
    fun from(
      path: Path,
      keepMetaFiles: Boolean,
    ): Classes {
      return Classes(GenericJarArchive.from(path, keepMetaFiles) ?: GenericJarArchive.NONE)
    }
  }
}
