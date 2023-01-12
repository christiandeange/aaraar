package sh.christian.aaraar

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import sh.christian.aaraar.model.ArtifactArchive
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
  abstract val prefix: Property<String>

  @get:Input
  abstract val packagesToShade: SetProperty<String>

  @get:Input
  abstract val packagesToRemove: SetProperty<String>

  @get:OutputFile
  abstract val outputAar: RegularFileProperty

  @TaskAction
  fun packageAar() {
    val inputAar = ArtifactArchive.from(inputAar.getPath())

    val finalAar = embedClasspath.asFileTree.files
      .asSequence()
      .map { ArtifactArchive.from(it.toPath()) }
      .fold(initial = inputAar, ArtifactArchive::plus)

    // TODO Shading

    val outputPath = outputAar.getPath().deleteIfExists()
    finalAar.writeTo(path = outputPath)
  }

  private fun RegularFileProperty.getPath(): Path {
    return get().asFile.toPath()
  }
}
