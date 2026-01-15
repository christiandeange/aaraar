package sh.christian.aaraar.packaging

import sh.christian.aaraar.Environment
import sh.christian.aaraar.gradle.ArtifactArchiveProcessor
import sh.christian.aaraar.merger.Glob
import sh.christian.aaraar.merger.MergeRules
import sh.christian.aaraar.merger.impl.AarArchiveMerger
import sh.christian.aaraar.merger.impl.AndroidManifestMerger
import sh.christian.aaraar.merger.impl.ApiJarMerger
import sh.christian.aaraar.merger.impl.ArtifactArchiveMerger
import sh.christian.aaraar.merger.impl.AssetsMerger
import sh.christian.aaraar.merger.impl.ClassesMerger
import sh.christian.aaraar.merger.impl.FileSetMerger
import sh.christian.aaraar.merger.impl.GenericJarArchiveMerger
import sh.christian.aaraar.merger.impl.JarArchiveMerger
import sh.christian.aaraar.merger.impl.JniMerger
import sh.christian.aaraar.merger.impl.LintRulesMerger
import sh.christian.aaraar.merger.impl.NavigationJsonMerger
import sh.christian.aaraar.merger.impl.NoJarArchiveMerger
import sh.christian.aaraar.merger.impl.ProguardMerger
import sh.christian.aaraar.merger.impl.PublicTxtMerger
import sh.christian.aaraar.merger.impl.RTxtMerger
import sh.christian.aaraar.merger.impl.ResourcesMerger
import sh.christian.aaraar.model.AarArchive
import sh.christian.aaraar.model.ArtifactArchive
import sh.christian.aaraar.model.JarArchive
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.packaging.ShadeConfigurationScope.All
import sh.christian.aaraar.packaging.ShadeConfigurationScope.AnyScope
import sh.christian.aaraar.packaging.ShadeConfigurationScope.DependencyScope
import sh.christian.aaraar.packaging.ShadeConfigurationScope.FilesScope
import sh.christian.aaraar.packaging.ShadeConfigurationScope.ProjectScope
import sh.christian.aaraar.shading.impl.AarArchiveShader
import sh.christian.aaraar.shading.impl.ClassesShader
import sh.christian.aaraar.shading.impl.GenericJarArchiveShader
import sh.christian.aaraar.shading.impl.JarArchiveShader
import sh.christian.aaraar.shading.impl.LibsShader
import java.io.File
import java.nio.file.Path

/**
 * Defines methods for the actual processing and merging of archive files.
 */
class Packager(
  private val environment: Environment,
  private val packagingEnvironment: PackagingEnvironment,
  private val shadeEnvironment: ShadeEnvironment,
  private val logger: PackagerLogger,
) {

  /**
   * Prepares the input archive for processing.
   *
   * This method applies shading rules to the input archive, if any are applicable.
   */
  fun prepareInputArchive(
    inputPath: Path,
    identifier: ShadeConfigurationScope,
  ): ArtifactArchive {
    logger.info("Merge base: $inputPath")

    val inputArchive = ArtifactArchive.from(inputPath, environment)
    return applyShading(
      path = inputPath,
      archive = inputArchive,
      identifier = identifier,
    )
  }

  /**
   * Prepares a dependency archive for processing.
   *
   * This method applies shading rules to the dependency archive, if any are applicable.
   */
  fun prepareDependencyArchive(
    archivePath: Path,
    identifier: ShadeConfigurationScope?,
  ): ArtifactArchive {
    logger.info("Processing scope id '$identifier'")
    logger.info("  Input file: $archivePath")

    val dependencyArchive = ArtifactArchive.from(archivePath, environment)
    return applyShading(
      path = archivePath,
      archive = dependencyArchive,
      identifier = identifier,
    )
  }

  /**
   * Merges the input archive with the provided dependency archives.
   *
   * This method applies the configured merge rules to the input and dependency archives. The result is a single
   * merged archive that contains the combined contents of all input archives.
   */
  fun mergeArchives(
    inputArchive: ArtifactArchive,
    dependencyArchives: List<ArtifactArchive>,
  ): ArtifactArchive {
    val jniLibsMergeRules = MergeRules(
      pickFirsts = packagingEnvironment.jniLibs.pickFirsts.toGlob(),
      merges = Glob.None,
      excludes = packagingEnvironment.jniLibs.excludes.toGlob(),
    )

    val resourceMergeRules = MergeRules(
      pickFirsts = packagingEnvironment.resources.pickFirsts.toGlob(),
      merges = packagingEnvironment.resources.merges.toGlob(),
      excludes = packagingEnvironment.resources.excludes.toGlob(),
    )

    val resourcesJarMerger = GenericJarArchiveMerger(resourceMergeRules)
    val resourcesFileSetMerger = FileSetMerger(resourcesJarMerger, resourceMergeRules)
    val jniLibsFileSetMerger = FileSetMerger(NoJarArchiveMerger, jniLibsMergeRules)

    val artifactArchiveMerger = ArtifactArchiveMerger(
      jarArchiveMerger = JarArchiveMerger(
        classesMerger = ClassesMerger(resourcesJarMerger),
      ),
      aarArchiveMerger = AarArchiveMerger(
        androidManifestMerger = AndroidManifestMerger(),
        classesAndLibsMerger = ClassesMerger(resourcesJarMerger),
        resourcesMerger = ResourcesMerger(),
        rTxtMerger = RTxtMerger(),
        publicTxtMerger = PublicTxtMerger(),
        assetsMerger = AssetsMerger(resourcesFileSetMerger),
        jniMerger = JniMerger(jniLibsFileSetMerger),
        proguardMerger = ProguardMerger(),
        lintRulesMerger = LintRulesMerger(resourcesJarMerger),
        navigationJsonMerger = NavigationJsonMerger(),
        apiJarMerger = ApiJarMerger(resourcesJarMerger),
      ),
    )

    return artifactArchiveMerger.merge(inputArchive, dependencyArchives)
  }

  fun postProcessing(
    archive: ArtifactArchive,
    postProcessorFactories: List<ArtifactArchiveProcessor.Factory>,
  ): ArtifactArchive {
    val postProcessors = postProcessorFactories.map { it.create() }

    var processedArchive = archive
    for (processor in postProcessors) {
      processedArchive = processor.process(processedArchive)
    }

    return processedArchive
  }

  fun applyShading(
    path: Path,
    archive: ArtifactArchive,
    identifier: ShadeConfigurationScope?,
  ): ArtifactArchive {
    val emptyConfiguration = ShadeConfiguration(
      classRenames = emptyMap(),
      classDeletes = emptySet(),
      resourceRenames = emptyMap(),
      resourceDeletes = emptySet(),
    )

    val shadeRules = shadeEnvironment.rules
      .filter { rule -> rule.scope.matches(path, identifier) }
      .fold(emptyConfiguration) { a, b ->
        ShadeConfiguration(
          classRenames = a.classRenames + b.configuration.classRenames,
          classDeletes = a.classDeletes + b.configuration.classDeletes,
          resourceRenames = a.resourceRenames + b.configuration.resourceRenames,
          resourceDeletes = a.resourceDeletes + b.configuration.resourceDeletes,
        )
      }

    return if (shadeRules.isEmpty()) {
      archive
    } else {
      logger.info("  Applying shading rules:")
      shadeRules.classRenames.forEach { (pattern, result) ->
        logger.info("    Rename class    '$pattern' → '$result'")
      }
      shadeRules.classDeletes.forEach { target ->
        logger.info("    Delete class    '$target'")
      }
      shadeRules.resourceRenames.forEach { (pattern, result) ->
        logger.info("    Rename resource '$pattern' → '$result'")
      }
      shadeRules.resourceDeletes.forEach { target ->
        logger.info("    Delete resource '$target'")
      }

      val genericJarArchiveShader = GenericJarArchiveShader()
      val classesShader = ClassesShader(genericJarArchiveShader)
      val libsShader = LibsShader(genericJarArchiveShader)
      when (archive) {
        is AarArchive -> AarArchiveShader(classesShader, libsShader).shade(archive, shadeRules)
        is JarArchive -> JarArchiveShader(classesShader).shade(archive, shadeRules)
      }
    }
  }

  private fun ShadeConfigurationScope.matches(
    path: Path,
    identifier: ShadeConfigurationScope?,
  ): Boolean = when (this) {
    is All -> true
    is DependencyScope -> identifier is DependencyScope && matches(identifier)
    is ProjectScope -> identifier is ProjectScope && matches(identifier)
    is FilesScope -> matches(path.toFile())
    is AnyScope -> scopes.any { it.matches(path, identifier) }
  }

  private fun DependencyScope.matches(identifier: DependencyScope): Boolean {
    val matchesGroup = group == identifier.group
    val matchesName = name == null || name == identifier.name
    val matchesVersion = version == null || version == identifier.version

    return matchesGroup && matchesName && matchesVersion
  }

  private fun ProjectScope.matches(identifier: ProjectScope): Boolean {
    return path == identifier.path
  }

  private fun FilesScope.matches(path: File): Boolean {
    return path in files
  }

  private fun Collection<String>.toGlob(): Glob {
    return map(Glob::fromString).fold(Glob.None, Glob::plus)
  }
}
