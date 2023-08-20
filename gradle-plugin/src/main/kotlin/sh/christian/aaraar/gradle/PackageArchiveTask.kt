package sh.christian.aaraar.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import sh.christian.aaraar.Environment
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
import sh.christian.aaraar.utils.deleteIfExists
import java.nio.file.Path

@CacheableTask
abstract class PackageArchiveTask : DefaultTask() {

  @get:InputFile
  @get:PathSensitive(RELATIVE)
  abstract val inputArchive: RegularFileProperty

  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  abstract val embedClasspath: ConfigurableFileCollection

  @get:Input
  abstract val shadeConfiguration: Property<ShadeConfiguration>

  @get:Input
  abstract val keepMetaFiles: Property<Boolean>

  @get:Input
  @get:Optional
  abstract val androidAaptIgnore: Property<String>

  @get:OutputFile
  abstract val outputArchive: RegularFileProperty

  @TaskAction
  fun packageArtifactArchive() {
    val environment = Environment(
      androidAaptIgnore = androidAaptIgnore.get(),
      keepClassesMetaFiles = keepMetaFiles.get(),
    )
    logger.info("Packaging environment: $environment")

    val inputPath = inputArchive.getPath()
    val input = ArtifactArchive.from(inputPath, environment)
    logger.info("Merge base: $inputPath")

    val dependencyArchives =
      embedClasspath.asFileTree.files
        .map {
          logger.info("  From ${it.toPath()}")
          ArtifactArchive.from(it.toPath(), environment)
        }

    val mergedArchive = mergeArchive(input, dependencyArchives)
    val shadingConfiguration = shadeConfiguration.get()

    val finalArchive = if (shadingConfiguration.isEmpty()) {
      logger.info("Skipping shading input since no rules are defined.")
      mergedArchive
    } else {
      logger.info("Shading input with rules:")
      shadingConfiguration.classRenames.forEach { (pattern, result) ->
        logger.info("    Rename class '$pattern' → '$result'")
      }
      shadingConfiguration.classDeletes.forEach { target ->
        logger.info("     Delete class '$target'")
      }
      shadingConfiguration.resourceExclusions.forEach { target ->
        logger.info("  Delete resource '$target'")
      }

      mergedArchive.shaded(shadingConfiguration)
    }

    val outputPath = outputArchive.getPath().deleteIfExists()
    logger.info("Merged into: $outputPath")
    finalArchive.writeTo(path = outputPath)
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
}
