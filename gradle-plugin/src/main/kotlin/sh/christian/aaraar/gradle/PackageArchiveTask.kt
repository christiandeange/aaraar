package sh.christian.aaraar.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.LibraryBinaryIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import sh.christian.aaraar.Environment
import sh.christian.aaraar.gradle.ShadeConfigurationScope.All
import sh.christian.aaraar.gradle.ShadeConfigurationScope.DependencyScope
import sh.christian.aaraar.gradle.ShadeConfigurationScope.ProjectScope
import sh.christian.aaraar.merger.Merger
import sh.christian.aaraar.merger.impl.AarArchiveMerger
import sh.christian.aaraar.merger.impl.AndroidManifestMerger
import sh.christian.aaraar.merger.impl.ArtifactArchiveMerger
import sh.christian.aaraar.merger.impl.AssetsMerger
import sh.christian.aaraar.merger.impl.ClassesMerger
import sh.christian.aaraar.merger.impl.FileSetMerger
import sh.christian.aaraar.merger.impl.GenericJarArchiveMerger
import sh.christian.aaraar.merger.impl.JarArchiveMerger
import sh.christian.aaraar.merger.impl.JniMerger
import sh.christian.aaraar.merger.impl.LintRulesMerger
import sh.christian.aaraar.merger.impl.NavigationJsonMerger
import sh.christian.aaraar.merger.impl.ProguardMerger
import sh.christian.aaraar.merger.impl.PublicTxtMerger
import sh.christian.aaraar.merger.impl.RTxtMerger
import sh.christian.aaraar.merger.impl.ResourcesMerger
import sh.christian.aaraar.model.ArtifactArchive
import sh.christian.aaraar.model.FileSet
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.ShadeConfiguration
import java.nio.file.Files
import java.nio.file.Path

@CacheableTask
abstract class PackageArchiveTask : DefaultTask() {

  @get:InputFile
  @get:PathSensitive(RELATIVE)
  abstract val inputArchive: RegularFileProperty

  @get:Classpath
  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  abstract val embedClasspath: Property<Configuration>

  @get:Input
  abstract val shadeEnvironment: Property<ShadeEnvironment>

  @get:Input
  abstract val keepMetaFiles: Property<Boolean>

  @get:OutputFile
  abstract val outputArchive: RegularFileProperty

  internal abstract fun environment(): Environment

  @TaskAction
  fun packageArtifactArchive() {
    val environment = environment()
    logger.info("Packaging environment: $environment")

    val componentMapping: Map<Path, ComponentIdentifier> = embedClasspath.get()
      .incoming.artifacts.resolvedArtifacts.get()
      .associate {
        it.file.toPath() to it.id.componentIdentifier
      }

    val inputPath = inputArchive.getPath()
    val shadeEnvironment = shadeEnvironment.get()

    logger.info("Merge base: $inputPath")
    val input = ArtifactArchive.from(inputPath, environment).applyShading(
      shadeEnvironment = shadeEnvironment,
      identifier = ProjectScope(identityPath.parent!!.toString()),
    )

    val dependencyArchives =
      componentMapping.keys
        .minus(inputPath)
        .map { archivePath ->
          val dependencyId = componentMapping[archivePath]
          logger.info("Processing $dependencyId")
          logger.info("  Input file: $archivePath")
          ArtifactArchive.from(archivePath, environment).applyShading(
            shadeEnvironment = shadeEnvironment,
            identifier = dependencyId?.toShadeConfigurationScope(),
          )
        }

    val mergedArchive = mergeArchive(input, dependencyArchives)

    val outputPath = outputArchive.getPath().apply { Files.deleteIfExists(this) }
    logger.info("Merged into: $outputPath")
    mergedArchive.writeTo(path = outputPath)
  }

  private fun mergeArchive(
    inputArchive: ArtifactArchive,
    dependencyArchives: List<ArtifactArchive>,
  ): ArtifactArchive {
    val jarMerger: Merger<GenericJarArchive> = GenericJarArchiveMerger()
    val fileSetMerger: Merger<FileSet> = FileSetMerger(jarMerger)
    val artifactArchiveMerger = ArtifactArchiveMerger(
      jarArchiveMerger = JarArchiveMerger(
        classesMerger = ClassesMerger(jarMerger),
      ),
      aarArchiveMerger = AarArchiveMerger(
        androidManifestMerger = AndroidManifestMerger(),
        classesAndLibsMerger = ClassesMerger(jarMerger),
        resourcesMerger = ResourcesMerger(),
        rTxtMerger = RTxtMerger(),
        publicTxtMerger = PublicTxtMerger(),
        assetsMerger = AssetsMerger(fileSetMerger),
        jniMerger = JniMerger(fileSetMerger),
        proguardMerger = ProguardMerger(),
        lintRulesMerger = LintRulesMerger(jarMerger),
        navigationJsonMerger = NavigationJsonMerger(),
      ),
    )

    return artifactArchiveMerger.merge(inputArchive, dependencyArchives)
  }

  private fun RegularFileProperty.getPath(): Path {
    return get().asFile.toPath()
  }

  private fun ArtifactArchive.applyShading(
    shadeEnvironment: ShadeEnvironment,
    identifier: ShadeConfigurationScope?,
  ): ArtifactArchive {
    val emptyConfiguration = ShadeConfiguration(
      classRenames = emptyMap(),
      classDeletes = emptySet(),
      resourceExclusions = emptySet(),
    )

    val shadeRules = shadeEnvironment.rules
      .filter { rule ->
        when (val applicableScope = rule.scope) {
          is All -> true
          is DependencyScope -> identifier is DependencyScope && applicableScope.matches(identifier)
          is ProjectScope -> identifier is ProjectScope && applicableScope.matches(identifier)
        }
      }.fold(emptyConfiguration) { a, b ->
        ShadeConfiguration(
          classRenames = a.classRenames + b.configuration.classRenames,
          classDeletes = a.classDeletes + b.configuration.classDeletes,
          resourceExclusions = a.resourceExclusions + b.configuration.resourceExclusions,
        )
      }

    return if (shadeRules.isEmpty()) {
      this
    } else {
      logger.info("  Applying shading rules:")
      shadeRules.classRenames.forEach { (pattern, result) ->
        logger.info("    Rename class '$pattern' → '$result'")
      }
      shadeRules.classDeletes.forEach { target ->
        logger.info("    Delete class '$target'")
      }
      shadeRules.resourceExclusions.forEach { target ->
        logger.info("    Remove file  '$target'")
      }

      shaded(shadeRules)
    }
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

  private fun ComponentIdentifier.toShadeConfigurationScope(): ShadeConfigurationScope? = when (this) {
    is ModuleComponentIdentifier -> DependencyScope(group, module, version)
    is ProjectComponentIdentifier -> ProjectScope(projectPath)
    is LibraryBinaryIdentifier -> null
    else -> null
  }
}
