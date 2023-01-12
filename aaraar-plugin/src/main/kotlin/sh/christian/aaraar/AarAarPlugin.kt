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

    val embed = project.configurations.create("embed") {
      isTransitive = false
      isCanBeConsumed = true
      isCanBeResolved = false
    }

    project.pluginManager.withPlugin("com.android.library") {
      val androidComponents = project.extensions.getByType<LibraryAndroidComponentsExtension>()

      androidComponents.onVariants { variant ->
        val variantEmbed = project.configurations.create("${variant.name}Embed") {
          isTransitive = false
          isCanBeConsumed = true
          isCanBeResolved = false
        }

        val variantEmbedClasspath = project.configurations.create("${variant.name}EmbedClasspath") {
          extendsFrom(embed)
          extendsFrom(variantEmbed)

          isTransitive = true
          isCanBeConsumed = false
          isCanBeResolved = true
        }

        val aar = variant.artifacts.get(SingleArtifact.AAR)
        val fileName = "${project.name}-${variant.name}.aar"
        val outFile = project.buildDir / FD_OUTPUTS / "aaraar" / fileName

        project.tasks.register<PackageAarTask>(variant.taskName("package", "Aar")) {
          inputAar.set(aar)
          embedClasspath.from(variantEmbedClasspath)
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
