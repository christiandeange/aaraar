package sh.christian.aaraar.gradle.agp

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.LibraryVariant
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.TaskProvider

internal class Agp8AndroidVariant(
  private val variant: LibraryVariant,
) : AndroidVariant {
  override val variantName: String = variant.name
  override val buildType: String? = variant.buildType
  override val namespace: String get() = variant.namespace.get()
  override val packaging: AndroidPackaging = Agp8AndroidPackaging(variant.packaging)

  override fun <T : Task> registerAarTransform(
    task: TaskProvider<T>,
    inputAar: (T) -> RegularFileProperty,
    outputAar: (T) -> RegularFileProperty,
  ) {
    variant.artifacts
      .use(task)
      .wiredWithFiles(inputAar, outputAar)
      .toTransform(SingleArtifact.AAR)
  }
}
