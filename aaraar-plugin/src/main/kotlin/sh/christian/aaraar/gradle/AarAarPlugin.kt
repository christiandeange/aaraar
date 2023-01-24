@file:Suppress("UnstableApiUsage")

package sh.christian.aaraar.gradle

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.attributes.BuildTypeAttr
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import sh.christian.aaraar.utils.div
import javax.inject.Inject

class AarAarPlugin
@Inject constructor(
  private val softwareComponentFactory: SoftwareComponentFactory,
) : Plugin<Project> {
  override fun apply(target: Project) {
    val project = target

    project.pluginManager.withPlugin("com.android.library") {
      val android = project.extensions.getByType<LibraryExtension>()
      val androidComponents = project.extensions.getByType<LibraryAndroidComponentsExtension>()

      project.dependencies.attributesSchema {
        attribute(ARTIFACT_TYPE_ATTRIBUTE) {
          compatibilityRules.add(ArtifactTypeCompatibilityDependencyRule::class)
          disambiguationRules.add(ArtifactTypeDisambiguationDependencyRule::class)
        }
      }

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
        val variantEmbedClasspath = project.configurations.create(
          variant.name(suffix = "EmbedClasspath")
        ) {
          extendsFrom(embed)
          variant.buildType?.let { buildType ->
            extendsFrom(project.configurations.getAt("${buildType}Embed"))
            attributes {
              attribute(BuildTypeAttr.ATTRIBUTE, project.objects.named(buildType))
            }
          }

          isTransitive = false
          isCanBeConsumed = false
          isCanBeResolved = true
          attributes {
            attribute(ARTIFACT_TYPE_ATTRIBUTE, MERGEABLE_ARTIFACT_TYPE)
          }
        }

        val embedAarConfiguration = project.configurations.create(variant.name(suffix = "EmbedAar")) {
          isTransitive = true
          isCanBeConsumed = true
          isCanBeResolved = true
        }

        val androidAaptIgnoreEnv =
          project.providers.environmentVariable("ANDROID_AAPT_IGNORE").orElse("")

        val aar = variant.artifacts.get(SingleArtifact.AAR)
        val fileName = "${project.name}-${variant.name}.aar"
        val outFile = project.buildDir / "outputs" / "aaraar" / fileName

        val packageVariantAar = project.tasks.register<PackageAarTask>(
          variant.name("package", "Aar")
        ) {
          inputAar.set(aar)
          embedClasspath.from(variantEmbedClasspath)
          classRenames.set(aaraar.classRenames)
          classDeletes.set(aaraar.classDeletes)
          keepMetaFiles.set(aaraar.keepMetaFiles)
          androidAaptIgnore.set(androidAaptIgnoreEnv)
          outputAar.set(outFile)
        }

        project.artifacts {
          add(embedAarConfiguration.name, outFile) {
            builtBy(packageVariantAar)
          }
        }

        with(softwareComponentFactory.adhoc(variant.name(suffix = "EmbedAar"))) {
          project.components.add(this)
          addVariantsFromConfiguration(embedAarConfiguration) {
            mapToMavenScope("runtime")
          }
          addVariantsFromConfiguration(variant.runtimeConfiguration) {
            mapToMavenScope("runtime")
          }
        }
      }
    }
  }

  private fun Variant.name(
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
