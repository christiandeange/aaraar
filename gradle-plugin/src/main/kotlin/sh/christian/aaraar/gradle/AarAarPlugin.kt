@file:Suppress("UnstableApiUsage")

package sh.christian.aaraar.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import sh.christian.aaraar.gradle.ShadeConfigurationScope.All
import sh.christian.aaraar.gradle.ShadeConfigurationScope.DependencyScope
import sh.christian.aaraar.gradle.ShadeConfigurationScope.FilesScope
import sh.christian.aaraar.gradle.ShadeConfigurationScope.ProjectScope
import sh.christian.aaraar.gradle.agp.AgpCompat
import sh.christian.aaraar.gradle.agp.AndroidVariant
import sh.christian.aaraar.model.ShadeConfiguration

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
    }

    val jarTask = tasks.named<Jar>("jar")

    val packageJar = tasks.register<PackageJarTask>("packageJar") {
      embedClasspath.set(embed)

      shadeEnvironment.set(parseShadeEnvironment(aaraar))
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
      }
    }

    val androidAaptIgnoreEnv = providers.environmentVariable("ANDROID_AAPT_IGNORE").orElse("")

    val packageVariantAar = tasks.register<PackageAarTask>(variant.name("package", "Aar")) {
      embedClasspath.set(variantEmbedClasspath)

      shadeEnvironment.set(parseShadeEnvironment(aaraar))
      keepMetaFiles.set(aaraar.keepMetaFiles)
      androidAaptIgnore.set(androidAaptIgnoreEnv)
      apiJarProcessorFactory.set(aaraar.apiJarProcessorFactory)
    }

    variant.registerAarTransform(
      packageVariantAar,
      PackageAarTask::inputAar,
      PackageAarTask::outputAar,
    )
  }

  private fun Project.parseShadeEnvironment(aaraar: AarAarExtension): ShadeEnvironment {
    val allConfigurations = buildList {
      add(aaraar.shading.allConfiguration)
      addAll(aaraar.shading.configurations.get())
    }

    val resourceExclusions = agp.android.packagingResourceExcludes()

    return ShadeEnvironment(
      rules = allConfigurations.map {
        ShadeConfigurationRule(
          scope = when (val selector = it.scopeSelector) {
            is ScopeSelector.All -> {
              All
            }

            is ScopeSelector.ForGroup -> {
              DependencyScope(selector.group, null, null)
            }

            is ScopeSelector.ForModule -> {
              dependencies.create(selector.dependency).let { dependency ->
                DependencyScope(dependency.group.orEmpty(), dependency.name, null)
              }
            }

            is ScopeSelector.ForDependency -> {
              dependencies.create(selector.dependency).let { dependency ->
                DependencyScope(dependency.group.orEmpty(), dependency.name, dependency.version)
              }
            }

            is ScopeSelector.ForProject -> {
              ProjectScope(selector.path)
            }

            is ScopeSelector.ForFiles -> {
              FilesScope(project.files(selector.files).files)
            }
          },
          configuration = ShadeConfiguration(
            classRenames = it.classRenames.get(),
            classDeletes = it.classDeletes.get(),
            resourceExclusions = resourceExclusions,
          ),
        )
      }
    )
  }
}
