@file:Suppress("UnstableApiUsage")

package sh.christian.aaraar.gradle

import com.android.SdkConstants.FD_OUTPUTS
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.attributes.BuildTypeAttr
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.utils.div
import javax.inject.Inject

/**
 * A plugin for creating a merged aar file. Configurable via the `aaraar` extension.
 * @see AarAarExtension
 */
@Suppress("unused")
class AarAarPlugin
@Inject constructor(
  private val softwareComponentFactory: SoftwareComponentFactory,
) : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    extensions.create("aaraar", AarAarExtension::class.java)

    pluginManager.withPlugin("com.android.library") {
      val android = extensions.getByType<LibraryExtension>()
      val androidComponents = extensions.getByType<LibraryAndroidComponentsExtension>()

      dependencies.attributesSchema {
        attribute(ARTIFACT_TYPE_ATTRIBUTE) {
          compatibilityRules.add(ArtifactTypeCompatibilityDependencyRule::class)
          disambiguationRules.add(ArtifactTypeDisambiguationDependencyRule::class)
        }
      }

      configurations.create("embed") {
        isTransitive = false
        isCanBeConsumed = true
        isCanBeResolved = false
      }

      android.buildTypes.configureEach {
        configurations.create("${name}Embed") {
          isTransitive = false
          isCanBeConsumed = true
          isCanBeResolved = false
        }
      }

      androidComponents.onVariants { variant ->
        applyPluginToVariant(variant)
      }
    }
  }

  private fun Project.applyPluginToVariant(variant: Variant) {
    val android = extensions.getByType<LibraryExtension>()
    val aaraar = extensions.getByType<AarAarExtension>()

    val variantEmbedClasspath = configurations.create(variant.name(suffix = "EmbedClasspath")) {
      extendsFrom(configurations.getAt("embed"))
      variant.buildType?.let { buildType ->
        extendsFrom(configurations.getAt("${buildType}Embed"))
        attributes {
          attribute(BuildTypeAttr.ATTRIBUTE, objects.named(buildType))
        }
      }

      isTransitive = false
      isCanBeConsumed = false
      isCanBeResolved = true
      attributes {
        attribute(ARTIFACT_TYPE_ATTRIBUTE, MERGEABLE_ARTIFACT_TYPE)
      }
    }

    val embedAarConfiguration = configurations.create(variant.name(suffix = "EmbedAar")) {
      isTransitive = true
      isCanBeConsumed = true
      isCanBeResolved = true

      attributes {
        attribute(USAGE_ATTRIBUTE, objects.named("embed-aar"))
      }
    }

    val androidAaptIgnoreEnv = providers.environmentVariable("ANDROID_AAPT_IGNORE").orElse("")

    val aar = variant.artifacts.get(SingleArtifact.AAR)
    val fileName = variant.name(prefix = "${project.name}-", suffix = ".aar")
    val outFile = buildDir / FD_OUTPUTS / "aaraar" / fileName

    val packageVariantAar = tasks.register<PackageAarTask>(variant.name("package", "Aar")) {
      inputAar.set(aar)
      embedClasspath.from(variantEmbedClasspath)

      shadeConfiguration.set(
        ShadeConfiguration(
          classRenames = aaraar.classRenames.get(),
          classDeletes = aaraar.classDeletes.get(),
          resourceExclusions = android.packagingOptions.resources.excludes.toSet(),
        )
      )
      keepMetaFiles.set(aaraar.keepMetaFiles)
      androidAaptIgnore.set(androidAaptIgnoreEnv)
      outputAar.set(outFile)
    }

    artifacts {
      add(embedAarConfiguration.name, outFile) {
        builtBy(packageVariantAar)
      }
    }

    val apiCompileElements = configurations.create(variant.name(suffix = "ApiCompileElements")) {
      extendsFrom(configurations.getAt("apiDependenciesMetadata"))
      attributes {
        attribute(USAGE_ATTRIBUTE, objects.named("pom-compile-elements"))
      }

      variant.buildType?.let { buildType ->
        extendsFrom(configurations.getAt("${buildType}ApiDependenciesMetadata"))
        attributes {
          attribute(BuildTypeAttr.ATTRIBUTE, objects.named(buildType))
        }
      }
    }

    with(softwareComponentFactory.adhoc(variant.name(suffix = "EmbedAar"))) {
      components.add(this)

      addVariantsFromConfiguration(apiCompileElements) {
        mapToMavenScope("compile")
      }
      addVariantsFromConfiguration(embedAarConfiguration) {
        mapToMavenScope("runtime")
      }
      addVariantsFromConfiguration(variant.runtimeConfiguration) {
        mapToMavenScope("runtime")
      }
    }
  }

  private fun Variant.name(
    prefix: String = "",
    suffix: String = "",
  ): String {
    return if (prefix.isEmpty()) {
      name + suffix
    } else if (prefix.last().isLetterOrDigit()) {
      prefix + name.capitalize() + suffix
    } else {
      prefix + name + suffix
    }
  }
}
