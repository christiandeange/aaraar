package sh.christian.aaraar

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

class AarAarPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val project = target

    val extension = project.extensions.create("aaraar", AarAarExtension::class.java)

    project.pluginManager.withPlugin("com.android.library") {
      val androidComponents = project.extensions.getByType<LibraryAndroidComponentsExtension>()

      androidComponents.onVariants { variant ->
        val aar = variant.artifacts.get(SingleArtifact.AAR)

        project.tasks.register<PackageAarTask>(variant.taskName("package", "Aar")) {
          inputAar.set(aar)
          prefix.set(extension.prefix)
          packagesToShade.set(extension.packagesToShade)
          packagesToRemove.set(extension.packagesToRemove)
        }
      }
    }
  }

  private fun Variant.taskName(
    prefix: String = "",
    suffix: String = "",
  ): String {
    return if (prefix.isEmpty()) {
      name + suffix
    } else {
      prefix + name.capitalize() + suffix
    }
  }
}
