package sh.christian.aaraar.gradle.agp

/**
 * Different types of artifacts.
 * These can be retrieved via [AndroidVariant.artifactFile].
 */
enum class FileArtifactType {
  /** The final AAR file as it would be published. */
  AAR,
}
