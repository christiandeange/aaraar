package sh.christian.aaraar.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask

@CacheableTask
abstract class PackageAarTask : PackageArchiveTask() {
  val inputAar: RegularFileProperty get() = inputArchive
  val outputAar: RegularFileProperty get() = outputArchive
}
