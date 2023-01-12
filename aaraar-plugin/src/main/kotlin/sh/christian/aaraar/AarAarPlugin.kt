package sh.christian.aaraar

import com.android.SdkConstants.FD_OUTPUTS
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import sh.christian.aaraar.utils.div

class AarAarPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val project = target

    val extension = project.extensions.create("aaraar", AarAarExtension::class.java)

    project.pluginManager.withPlugin("com.android.library") {
      val androidComponents = project.extensions.getByType<LibraryAndroidComponentsExtension>()

      androidComponents.onVariants { variant ->
        val aar = variant.artifacts.get(SingleArtifact.AAR)
        val outFile = project.buildDir / FD_OUTPUTS / "aaraar-${variant.name}.aar"

        project.tasks.register<PackageAarTask>(variant.taskName("package", "Aar")) {
          inputAar.set(aar)
          prefix.set(extension.prefix)
          packagesToShade.set(extension.packagesToShade)
          packagesToRemove.set(extension.packagesToRemove)
          outputAar.set(outFile)
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
