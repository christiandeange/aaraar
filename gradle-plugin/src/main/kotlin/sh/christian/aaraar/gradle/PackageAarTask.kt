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
import sh.christian.aaraar.model.AarArchive
import sh.christian.aaraar.model.ArtifactArchive
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.utils.deleteIfExists
import java.nio.file.Path

@CacheableTask
abstract class PackageAarTask : DefaultTask() {

  @get:InputFile
  @get:PathSensitive(RELATIVE)
  abstract val inputAar: RegularFileProperty

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
  abstract val outputAar: RegularFileProperty

  @TaskAction
  fun packageAar() {
    val environment = Environment(
      androidAaptIgnore = androidAaptIgnore.get(),
      keepClassesMetaFiles = keepMetaFiles.get(),
    )
    logger.info("Packaging environment: $environment")

    val inputAarPath = inputAar.getPath()
    val inputAar = ArtifactArchive.from(inputAarPath, environment) as AarArchive
    logger.info("Merge base AAR: $inputAarPath")

    val dependencyArchives =
      embedClasspath.asFileTree.files
        .map {
          logger.info("  From ${it.toPath()}")
          ArtifactArchive.from(it.toPath(), environment)
        }

    val mergedArchive = inputAar.mergeWith(dependencyArchives)
    val shadingConfiguration = shadeConfiguration.get()

    val finalArchive = if (shadingConfiguration.isEmpty()) {
      logger.info("Skipping shading input AAR since no rules are defined.")
      mergedArchive
    } else {
      logger.info("Shading input AAR with rules:")
      shadingConfiguration.classRenames.forEach { (pattern, result) ->
        logger.info("    Rename class '$pattern' ??? '$result'")
      }
      shadingConfiguration.classDeletes.forEach { target ->
        logger.info("     Delete class '$target'")
      }
      shadingConfiguration.resourceExclusions.forEach { target ->
        logger.info("  Delete resource '$target'")
      }

      mergedArchive.shaded(shadingConfiguration)
    }

    val outputPath = outputAar.getPath().deleteIfExists()
    logger.info("Merge AAR into: $outputPath")
    finalArchive.writeTo(path = outputPath)
  }

  private fun RegularFileProperty.getPath(): Path {
    return get().asFile.toPath()
  }
}
