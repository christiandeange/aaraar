@file:Suppress("UnstableApiUsage")

package sh.christian.aaraar.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import sh.christian.aaraar.gradle.agp.AgpCompat
import sh.christian.aaraar.gradle.agp.AndroidVariant
import sh.christian.aaraar.model.ShadeConfiguration

/**
 * A plugin for creating a merged aar file. Configurable via the `aaraar` extension.
 * @see AarAarExtension
 */
class AarAarPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    val aaraar = extensions.create("aaraar", AarAarExtension::class.java)

    pluginManager.withPlugin("com.android.library") {
      val agp = project.agp

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

      agp.android.onBuildTypes { buildType ->
        configurations.create("${buildType}Embed") {
          isTransitive = false
          isCanBeConsumed = true
          isCanBeResolved = false
        }
      }

      agp.onVariants { variant ->
        val isEnabledForVariant = aaraar.isEnabledForVariant.apply { disallowChanges() }.get()
        if (isEnabledForVariant(VariantDescriptor(variant.variantName, variant.buildType))) {
          applyPluginToVariant(agp, variant)
        } else {
          logger.info("aaraar packaging disabled for ${variant.variantName}, skipping...")
        }
      }
    }
  }

  private fun Project.applyPluginToVariant(
    agp: AgpCompat,
    variant: AndroidVariant,
  ) {
    val aaraar = extensions.getByType<AarAarExtension>()

    val variantEmbedClasspath = configurations.create(variant.name(suffix = "EmbedClasspath")) {
      extendsFrom(configurations.getAt("embed"))
      variant.buildType?.let { buildType ->
        extendsFrom(configurations.getAt("${buildType}Embed"))
        attributes {
          with(agp) { buildTypeAttribute(buildType) }
        }
      }

      isTransitive = false
      isCanBeConsumed = false
      isCanBeResolved = true
      attributes {
        attribute(ARTIFACT_TYPE_ATTRIBUTE, MERGEABLE_ARTIFACT_TYPE)
      }
    }

    val androidAaptIgnoreEnv = providers.environmentVariable("ANDROID_AAPT_IGNORE").orElse("")

    val packageVariantAar = tasks.register<PackageAarTask>(variant.name("package", "Aar")) {
      embedClasspath.from(variantEmbedClasspath)

      shadeConfiguration.set(
        ShadeConfiguration(
          classRenames = aaraar.classRenames.get(),
          classDeletes = aaraar.classDeletes.get(),
          resourceExclusions = agp.android.packagingResourceExcludes(),
        )
      )
      keepMetaFiles.set(aaraar.keepMetaFiles)
      androidAaptIgnore.set(androidAaptIgnoreEnv)
    }

    variant.registerAarTransform(
      packageVariantAar,
      PackageAarTask::inputAar,
      PackageAarTask::outputAar,
    )
  }
}
