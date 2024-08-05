package sh.christian.aaraar.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import sh.christian.aaraar.Environment

@CacheableTask
abstract class PackageAarTask : PackageArchiveTask() {
  @get:InputFile
  @get:PathSensitive(PathSensitivity.RELATIVE)
  val inputAar: RegularFileProperty get() = inputArchive

  @get:OutputFile
  val outputAar: RegularFileProperty get() = outputArchive

  @get:Input
  @get:Optional
  abstract val androidAaptIgnore: Property<String>

  final override fun environment(): Environment {
    return Environment(
      androidAaptIgnore = androidAaptIgnore.get(),
      keepClassesMetaFiles = keepMetaFiles.get(),
    )
  }
}
