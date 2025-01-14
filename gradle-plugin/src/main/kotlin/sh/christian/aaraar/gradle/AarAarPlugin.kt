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
import sh.christian.aaraar.packaging.ShadeConfigurationScope
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

      // Match incoming dependencies and tag outgoing artifacts as targeting the JVM.
      attributes.attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
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

    embed.outgoing {
      // Outgoing artifact is a merged jar (which is still considered mergeable!)
      artifact(packageJar.flatMap { it.outputJar })
      attributes.attribute(ARTIFACT_TYPE_ATTRIBUTE, MERGED_ARTIFACT_TYPE)
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

        // Add build type attribute to match incoming dependencies and tag outgoing artifacts.
        with(agp) { attributes.buildTypeAttribute(buildType) }
      }

      isTransitive = false
      isCanBeConsumed = true
      isCanBeResolved = true

      // Match incoming dependencies and tag outgoing artifacts as targeting Android.
      attributes.attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.ANDROID))

      // Incoming dependencies should be mergeable artifacts as per ArtifactTypeCompatibilityDependencyRule.
      incoming.attributes.attribute(ARTIFACT_TYPE_ATTRIBUTE, MERGEABLE_ARTIFACT_TYPE)
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

    variantEmbedClasspath.outgoing {
      // Outgoing artifact is a merged aar (which is still considered mergeable!)
      artifact(packageVariantAar.flatMap { it.outputAar })
      attributes.attribute(ARTIFACT_TYPE_ATTRIBUTE, MERGED_ARTIFACT_TYPE)
    }
  }

  private fun parseShadeEnvironment(
    aaraar: AarAarExtension,
    variant: AndroidVariant?,
  ): ShadeEnvironment {
    return ShadeEnvironment(
      rules = buildList {
        if (variant != null) {
          val resourceExclusions = variant.packaging.resources.excludes.getOrElse(emptySet())

          add(
            ShadeConfigurationRule(
              scope = ShadeConfigurationScope.All,
              configuration = ShadeConfiguration(
                // Rename all embedded R class references to use the namespace of the merged aar.
                classRenames = mapOf("**.R\$*" to "${variant.namespace}.R\$@2"),
                // Delete those embedded R classes from the merged aar.
                classDeletes = setOf("**.R\$*", "**.R"),
                resourceExclusions = resourceExclusions,
              ),
            )
          )
        }

        val allConfigurations = aaraar.shading.configurations.get()

        addAll(
          allConfigurations.map {
            ShadeConfigurationRule(
              scope = it.scopeSelector,
              configuration = ShadeConfiguration(
                classRenames = it.classRenames.get(),
                classDeletes = it.classDeletes.get(),
                resourceExclusions = emptySet(),
              ),
            )
          }
        )
      },
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
