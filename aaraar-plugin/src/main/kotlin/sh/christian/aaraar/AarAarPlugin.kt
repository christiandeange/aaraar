package sh.christian.aaraar

import com.android.SdkConstants.FD_OUTPUTS
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.dsl.LibraryExtension
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

    project.pluginManager.withPlugin("com.android.library") {
      val android = project.extensions.getByType<LibraryExtension>()
      val androidComponents = project.extensions.getByType<LibraryAndroidComponentsExtension>()

      val aaraar = project.extensions.create("aaraar", AarAarExtension::class.java)

      val embed = project.configurations.create("embed") {
        isTransitive = false
        isCanBeConsumed = true
        isCanBeResolved = false
      }

      android.buildTypes.configureEach {
        project.configurations.create("${name}Embed") {
          isTransitive = false
          isCanBeConsumed = true
          isCanBeResolved = false
        }
      }

      androidComponents.onVariants { variant ->
        val variantEmbedClasspath = project.configurations.create("${variant.name}EmbedClasspath") {
          extendsFrom(embed)
          variant.buildType?.let { buildType ->
            extendsFrom(project.configurations.getAt("${buildType}Embed"))
          }

          isTransitive = true
          isCanBeConsumed = false
          isCanBeResolved = true
        }

        val androidAaptIgnoreEnv =
          project.providers.environmentVariable("ANDROID_AAPT_IGNORE").orElse("")

        val aar = variant.artifacts.get(SingleArtifact.AAR)
        val fileName = "${project.name}-${variant.name}.aar"
        val outFile = project.buildDir / FD_OUTPUTS / "aaraar" / fileName

        project.tasks.register<PackageAarTask>(variant.taskName("package", "Aar")) {
          inputAar.set(aar)
          embedClasspath.from(variantEmbedClasspath)
          classRenames.set(aaraar.classRenames)
          classDeletes.set(aaraar.classDeletes)
          androidAaptIgnore.set(androidAaptIgnoreEnv)
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
