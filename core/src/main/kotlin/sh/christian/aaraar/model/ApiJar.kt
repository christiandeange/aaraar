package sh.christian.aaraar.model

import java.nio.file.Path

/**
 * Represents the contents of the `api.jar` file, the IDE sources for an [ArtifactArchive].
 */
class ApiJar(
  val archive: GenericJarArchive,
) {
  fun writeTo(path: Path) {
    archive.writeTo(path)
  }

  companion object {
    fun from(
      path: Path,
      keepMetaFiles: Boolean,
    ): ApiJar {
      return ApiJar(GenericJarArchive.from(path, keepMetaFiles) ?: GenericJarArchive.NONE)
    }
  }
}
