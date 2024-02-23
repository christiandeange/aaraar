package sh.christian.aaraar.model

import java.nio.file.Path

/**
 * Represents the contents of the `api.jar` file, the IDE sources for an [ArtifactArchive].
 */
class ApiJar
internal constructor(
  val archive: GenericJarArchive,
) {
  fun writeTo(path: Path) {
    archive.writeTo(path)
  }

  companion object {
    fun from(jarArchive: GenericJarArchive): ApiJar = ApiJar(jarArchive)

    fun from(
      path: Path,
      keepMetaFiles: Boolean,
    ): ApiJar {
      return from(GenericJarArchive.from(path, keepMetaFiles) ?: GenericJarArchive.NONE)
    }
  }
}
