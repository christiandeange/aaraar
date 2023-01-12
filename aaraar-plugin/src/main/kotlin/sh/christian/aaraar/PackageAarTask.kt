package sh.christian.aaraar

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class PackageAarTask : DefaultTask() {

  @get:InputFile
  @get:PathSensitive(RELATIVE)
  abstract val inputAar: RegularFileProperty

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
    println("inputAar = ${inputAar.get().asFile.absolutePath}")
    println("prefix = ${prefix.get()}")
    println("packagesToShade = ${packagesToShade.get()}")
    println("packagesToRemove = ${packagesToRemove.get()}")
    println("outputAar = ${outputAar.get().asFile.absolutePath}")
  }
}
