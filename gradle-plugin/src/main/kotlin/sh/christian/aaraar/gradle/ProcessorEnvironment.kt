package sh.christian.aaraar.gradle

import sh.christian.aaraar.Environment
import sh.christian.aaraar.model.ArtifactArchive
import java.nio.file.Path

/**
 * The environment provided to an [ArtifactArchiveProcessor] when processing an [ArtifactArchive].
 *
 * This includes additional context about the input archive and its dependencies that a post-processor may want to use
 * when processing the archive.
 *
 * @param environment general properties that influence the archive merging process.
 * @param inputArchive the input classpath of the project producing the merged archive.
 * @param compileClasspath the compilation classpath of the project producing the merged archive, minus [inputArchive].
 * @param embedClasspath the embed classpath of the project producing the merged archive.
 */
class ProcessorEnvironment(
  val environment: Environment,
  val inputArchive: ArtifactArchive,
  val compileClasspath: List<Path>,
  val embedClasspath: List<Path>,
)
