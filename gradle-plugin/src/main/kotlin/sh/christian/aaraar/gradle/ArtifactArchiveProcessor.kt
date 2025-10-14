package sh.christian.aaraar.gradle

import sh.christian.aaraar.model.ArtifactArchive
import java.io.Serializable

/**
 * Observes or modifies a merged [ArtifactArchive]. This is useful for post-processing the merged archive to optionally
 * add, remove, modify, validate, or inspect the contents of the archive.
 */
fun interface ArtifactArchiveProcessor {
  fun process(
    environment: ProcessorEnvironment,
    archive: ArtifactArchive,
  ): ArtifactArchive

  /** Factory for creating a new [ArtifactArchiveProcessor]. */
  interface Factory : Serializable {
    fun create(): ArtifactArchiveProcessor
  }
}
