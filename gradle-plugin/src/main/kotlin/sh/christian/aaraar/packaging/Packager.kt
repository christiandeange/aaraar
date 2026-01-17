package sh.christian.aaraar.packaging

import sh.christian.aaraar.Environment
import sh.christian.aaraar.gradle.ArtifactArchiveProcessor
import sh.christian.aaraar.model.ArtifactArchive
import java.io.Serializable
import java.nio.file.Path

/**
 * Defines methods for the actual processing and merging of archive files.
 */
interface Packager {
  /**
   * Prepares the input archive for processing.
   *
   * This method applies shading rules to the input archive, if any are applicable.
   */
  fun prepareInputArchive(
    inputPath: Path,
    identifier: ShadeConfigurationScope,
  ): ArtifactArchive

  /**
   * Prepares a dependency archive for processing.
   *
   * This method applies shading rules to the dependency archive, if any are applicable.
   */
  fun prepareDependencyArchive(
    archivePath: Path,
    identifier: ShadeConfigurationScope?,
  ): ArtifactArchive

  /**
   * Applies shading rules to the given archive.
   *
   * This method checks if there are any shading rules applicable to the provided [ShadeConfigurationScope]
   */
  fun applyShading(
    path: Path,
    archive: ArtifactArchive,
    identifier: ShadeConfigurationScope?,
  ): ArtifactArchive

  /**
   * Merges the input archive with the provided dependency archives.
   *
   * This method applies the configured merge rules to the input and dependency archives. The result is a single
   * merged archive that contains the combined contents of all input archives.
   */
  fun mergeArchives(
    inputArchive: ArtifactArchive,
    dependencyArchives: List<ArtifactArchive>,
  ): ArtifactArchive

  /**
   * Applies post-processing to the merged archive.
   */
  fun postProcessing(
    archive: ArtifactArchive,
    postProcessorFactories: List<ArtifactArchiveProcessor.Factory>,
  ): ArtifactArchive

  interface Factory : Serializable {
    fun create(
      environment: Environment,
      packagingEnvironment: PackagingEnvironment,
      shadeEnvironment: ShadeEnvironment,
      logger: PackagerLogger,
    ): Packager
  }
}
