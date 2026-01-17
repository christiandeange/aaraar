package sh.christian.aaraar.gradle

import com.android.utils.mapValuesNotNull
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.LibraryBinaryIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.component.local.model.OpaqueComponentArtifactIdentifier
import sh.christian.aaraar.Environment
import sh.christian.aaraar.model.ArtifactArchive
import sh.christian.aaraar.packaging.Packager
import sh.christian.aaraar.packaging.PackagingEnvironment
import sh.christian.aaraar.packaging.ShadeConfigurationScope
import sh.christian.aaraar.packaging.ShadeConfigurationScope.DependencyScope
import sh.christian.aaraar.packaging.ShadeConfigurationScope.FilesScope
import sh.christian.aaraar.packaging.ShadeConfigurationScope.ProjectScope
import sh.christian.aaraar.packaging.ShadeEnvironment
import java.nio.file.Files
import java.nio.file.Path

@CacheableTask
abstract class PackageArchiveTask : DefaultTask() {

  @get:InputFile
  @get:PathSensitive(RELATIVE)
  abstract val inputArchive: RegularFileProperty

  @get:Internal
  abstract val embedArtifacts: Property<ArtifactCollection>

  @get:Input
  abstract val shadeEnvironment: Property<ShadeEnvironment>

  @get:Input
  abstract val packagingEnvironment: Property<PackagingEnvironment>

  @get:Input
  abstract val keepMetaFiles: Property<Boolean>

  @get:Input
  abstract val packagerFactory: Property<Packager.Factory>

  @get:Input
  abstract val postProcessorFactories: ListProperty<ArtifactArchiveProcessor.Factory>

  @get:OutputFile
  abstract val outputArchive: RegularFileProperty

  /** Used to track the [ArtifactCollection] file inputs, only queried by Gradle for task/configuration caching. */
  @Suppress("unused")
  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  val embedArtifactFiles: FileCollection get() = embedArtifacts.get().artifactFiles

  internal abstract fun environment(): Environment

  @TaskAction
  fun packageArtifactArchive() {
    val environment = environment()
    logger.info("Packaging environment: $environment")

    val shadeEnvironment = shadeEnvironment.get()
    logger.info("Shading environment: $shadeEnvironment")

    val scopeMapping: Map<Path, ShadeConfigurationScope> = embedArtifacts.get()
      .resolvedArtifacts.get()
      .associate { it.file.toPath() to it.id.componentIdentifier.toShadeConfigurationScope() }
      .mapValuesNotNull { it.value }

    val packager = packagerFactory.get().create(
      environment = environment,
      packagingEnvironment = packagingEnvironment.get(),
      shadeEnvironment = shadeEnvironment,
      logger = GradlePackagerLogger(logger),
    )

    val inputPath = inputArchive.getPath()

    val input: ArtifactArchive = packager.prepareInputArchive(
      inputPath = inputPath,
      identifier = ProjectScope(identityPath.parent!!.toString()),
    )

    val dependencyArchives: List<ArtifactArchive> =
      scopeMapping.entries
        .filter { it.key != inputPath }
        .map { (archivePath, identifier) -> packager.prepareDependencyArchive(archivePath, identifier) }

    val mergedArchive = packager.mergeArchives(input, dependencyArchives)
    val finalizedArchive = packager.postProcessing(mergedArchive, postProcessorFactories.get())

    val outputPath = outputArchive.getPath().apply { Files.deleteIfExists(this) }
    logger.info("Merged into: $outputPath")
    finalizedArchive.writeTo(path = outputPath)
  }

  private fun RegularFileProperty.getPath(): Path {
    return get().asFile.toPath()
  }

  private fun ComponentIdentifier.toShadeConfigurationScope(): ShadeConfigurationScope? = when (this) {
    is ModuleComponentIdentifier -> DependencyScope(group, module, version)
    is ProjectComponentIdentifier -> ProjectScope(projectPath)
    is OpaqueComponentArtifactIdentifier -> FilesScope(setOf(file))
    is LibraryBinaryIdentifier -> null
    else -> null
  }
}
