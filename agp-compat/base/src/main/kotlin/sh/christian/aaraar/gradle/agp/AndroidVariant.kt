package sh.christian.aaraar.gradle.agp

import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

/**
 * A facade of some of the interactions with Android module variants.
 */
interface AndroidVariant {
  /**
   * The name of the variant.
   */
  val variantName: String

  /**
   * The name of the variant's build type, if present.
   */
  val buildType: String?

  /**
   * Access to the variant's compile [Configuration].
   * The returned [Configuration] should not be resolved until execution time.
   */
  val compileConfiguration: Configuration

  /**
   * Access to the variant's runtime [Configuration].
   * The returned [Configuration] should not be resolved until execution time.
   */
  val runtimeConfiguration: Configuration

  /**
   * Access to the artifacts on a Variant object.
   * These are temporary or final files that are produced by AGP during the build process.
   */
  fun artifactFile(type: FileArtifactType): Provider<RegularFile>

  fun name(
    prefix: String = "",
    suffix: String = "",
  ): String {
    return if (prefix.isEmpty()) {
      variantName + suffix
    } else if (prefix.last().isLetterOrDigit()) {
      prefix + variantName.capitalize() + suffix
    } else {
      prefix + variantName + suffix
    }
  }
}
