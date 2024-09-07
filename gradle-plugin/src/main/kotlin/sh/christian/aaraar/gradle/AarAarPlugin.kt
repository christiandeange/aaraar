@file:Suppress("UnstableApiUsage")

package sh.christian.aaraar.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.attributes.java.TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import sh.christian.aaraar.gradle.agp.AgpCompat
import sh.christian.aaraar.gradle.agp.AndroidPackaging
import sh.christian.aaraar.gradle.agp.AndroidVariant
import sh.christian.aaraar.model.ShadeConfiguration
import sh.christian.aaraar.packaging.PackagingEnvironment
import sh.christian.aaraar.packaging.PackagingEnvironment.JniLibs
import sh.christian.aaraar.packaging.PackagingEnvironment.Resources
import sh.christian.aaraar.packaging.ShadeConfigurationRule
import sh.christian.aaraar.packaging.ShadeEnvironment

/**
 * A plugin for creating a merged `aar` or `jar` file. Configurable via the `aaraar` extension.
 * @see AarAarExtension
 */
class AarAarPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    val aaraar = extensions.create("aaraar", AarAarExtension::class.java)

    pluginManager.withPlugin("java") {
      applyPluginToJavaLibrary()
    }

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
        val isEnabledForVariant = aaraar.variantFilter.apply { disallowChanges() }.get()
        if (isEnabledForVariant(VariantDescriptor(variant.variantName, variant.buildType))) {
          applyPluginToAndroidVariant(agp, variant)
        } else {
          logger.info("aaraar packaging disabled for ${variant.variantName}, skipping...")
        }
      }
    }
  }

  private fun Project.applyPluginToJavaLibrary() {
    val aaraar = extensions.getByType<AarAarExtension>()

    val embed = configurations.create("embed") {
      isTransitive = false
      isCanBeConsumed = true
      isCanBeResolved = true
      attributes {
        attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
      }
    }

    val jarTask = tasks.named<Jar>("jar")

    val packageJar = tasks.register<PackageJarTask>("packageJar") {
      embedClasspath.set(embed)

      shadeEnvironment.set(parseShadeEnvironment(aaraar, variant = null))
      packagingEnvironment.set(PackagingEnvironment.None)
      keepMetaFiles.set(aaraar.keepMetaFiles)

      inputJar.set(jarTask.flatMap { it.archiveFile })
      outputJar.set(jarTask.flatMap { it.archiveFile })
    }

    // These tasks are created by the `maven-publish` plugin.
    tasks.withType<GenerateModuleMetadata>().configureEach {
      dependsOn(packageJar)
    }
    tasks.withType<AbstractPublishToMaven>().configureEach {
      dependsOn(packageJar)
    }
  }

  private fun Project.applyPluginToAndroidVariant(
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
        attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.ANDROID))
      }
    }

    val androidAaptIgnoreEnv = providers.environmentVariable("ANDROID_AAPT_IGNORE").orElse("")

    val packageVariantAar = tasks.register<PackageAarTask>(variant.name("package", "Aar")) {
      embedClasspath.set(variantEmbedClasspath)

      shadeEnvironment.set(parseShadeEnvironment(aaraar, variant))
      packagingEnvironment.set(parsePackagingEnvironment(variant))
      keepMetaFiles.set(aaraar.keepMetaFiles)
      androidAaptIgnore.set(androidAaptIgnoreEnv)
      postProcessorFactories.set(aaraar.postProcessorFactories)
    }

    variant.registerAarTransform(
      packageVariantAar,
      PackageAarTask::inputAar,
      PackageAarTask::outputAar,
    )
  }

  private fun parseShadeEnvironment(
    aaraar: AarAarExtension,
    variant: AndroidVariant?,
  ): ShadeEnvironment {
    val allConfigurations = aaraar.shading.configurations.get()

    val resourceExclusions = variant?.packaging?.resources?.excludes?.orNull.orEmpty()

    return ShadeEnvironment(
      rules = allConfigurations.map {
        ShadeConfigurationRule(
          scope = it.scopeSelector,
          configuration = ShadeConfiguration(
            classRenames = it.classRenames.get(),
            classDeletes = it.classDeletes.get(),
            resourceExclusions = resourceExclusions,
          ),
        )
      }
    )
  }

  private fun parsePackagingEnvironment(variant: AndroidVariant): PackagingEnvironment {
    val packaging: AndroidPackaging = variant.packaging
    val jniLibs: AndroidPackaging.JniLibs = packaging.jniLibs
    val resources: AndroidPackaging.Resources = packaging.resources

    return PackagingEnvironment(
      jniLibs = JniLibs(
        pickFirsts = jniLibs.pickFirsts.get(),
        excludes = jniLibs.excludes.get(),
      ),
      resources = Resources(
        pickFirsts = resources.pickFirsts.get(),
        merges = resources.merges.get(),
        excludes = resources.excludes.get(),
      ),
    )
  }
}
