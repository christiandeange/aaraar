package sh.christian.aaraar.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import sh.christian.aaraar.Environment

@CacheableTask
abstract class PackageJarTask : PackageArchiveTask() {
  @get:InputFile
  @get:PathSensitive(PathSensitivity.RELATIVE)
  val inputJar: RegularFileProperty get() = inputArchive

  @get:OutputFile
  val outputJar: RegularFileProperty get() = outputArchive

  final override fun environment(): Environment {
    return Environment(
      androidAaptIgnore = "",
      keepClassesMetaFiles = keepMetaFiles.get(),
    )
  }
}
