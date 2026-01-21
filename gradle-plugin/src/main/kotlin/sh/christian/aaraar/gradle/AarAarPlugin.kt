@file:Suppress("UnstableApiUsage")

package sh.christian.aaraar.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.attributes.java.TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.create
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
    extensions.create<AarAarExtension>("aaraar")

    pluginManager.withPlugin("java") {
      applyPluginToJavaLibrary()
    }
    pluginManager.withPlugin("com.android.library") {
      applyPluginToAndroidLibrary()
    }
  }

  private fun Project.applyPluginToJavaLibrary() {
    val aaraar = extensions.getByType<AarAarExtension>()

    val embed = configurations.create("embed") {
      setTransitivity(false)
      isCanBeConsumed = true
      isCanBeResolved = false
    }

    val embedTree = configurations.create("embedTree") {
      setTransitivity(true)
      isCanBeConsumed = true
      isCanBeResolved = false
    }

    val classpath = configurations.create("embedClasspath") {
      extendsFrom(embed)
      extendsFrom(embedTree)

      setTransitivity(true)
      isCanBeConsumed = false
      isCanBeResolved = true

      // Match incoming dependencies as targeting the JVM.
      incoming.attributes.attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
      incoming.attributes.attribute(USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))

      // Incoming dependencies should be mergeable artifacts as per ArtifactTypeCompatibilityDependencyRule.
      incoming.attributes.attribute(ARTIFACT_TYPE_ATTRIBUTE, MERGEABLE_ARTIFACT_TYPE)
    }

    val jarTask = tasks.named<Jar>("jar")

    val packageJar = tasks.register<PackageJarTask>("packageJar") {
      embedArtifacts.set(classpath.incoming.artifacts)

      shadeEnvironment.set(parseShadeEnvironment(aaraar, variant = null))
      packagingEnvironment.set(PackagingEnvironment.None)
      keepMetaFiles.set(aaraar.keepMetaFiles)
      packagerFactory.set(aaraar.packagerFactory)

      inputJar.set(jarTask.flatMap { it.archiveFile })
      outputJar.set(jarTask.flatMap { it.archiveFile })
    }

    configurations.create("mergedJar") {
      setTransitivity(false)
      isCanBeConsumed = true
      isCanBeResolved = true

      outgoing {
        // Tag outgoing artifacts as targeting the JVM.
        attributes.attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
        attributes.attribute(USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))

        // Outgoing artifact is a merged jar (which is still considered mergeable!)
        attributes.attribute(ARTIFACT_TYPE_ATTRIBUTE, MERGED_JAR_TYPE)
        artifact(packageJar.flatMap { it.outputJar })
      }
    }

    // These tasks are created by the `maven-publish` plugin.
    tasks.withType<GenerateModuleMetadata>().configureEach {
      dependsOn(packageJar)
    }
    tasks.withType<AbstractPublishToMaven>().configureEach {
      dependsOn(packageJar)
    }
  }

  private fun Project.applyPluginToAndroidLibrary() {
    val aaraar = extensions.getByType<AarAarExtension>()
    val agp = project.agp

    dependencies.attributesSchema {
      attribute(ARTIFACT_TYPE_ATTRIBUTE) {
        compatibilityRules.add(ArtifactTypeCompatibilityDependencyRule::class)
        disambiguationRules.add(ArtifactTypeDisambiguationDependencyRule::class)
      }
    }

    configurations.create("embed") {
      setTransitivity(false)
      isCanBeConsumed = true
      isCanBeResolved = false
    }

    configurations.create("embedTree") {
      setTransitivity(true)
      isCanBeConsumed = true
      isCanBeResolved = false
    }

    agp.android.onBuildTypes { buildType ->
      configurations.create("${buildType}Embed") {
        setTransitivity(false)
        isCanBeConsumed = true
        isCanBeResolved = false
      }

      configurations.create("${buildType}EmbedTree") {
        setTransitivity(true)
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

  private fun Project.applyPluginToAndroidVariant(
    agp: AgpCompat,
    variant: AndroidVariant,
  ) {
    val aaraar = extensions.getByType<AarAarExtension>()

    val variantEmbedClasspath = configurations.create(variant.name(suffix = "EmbedClasspath")) {
      extendsFrom(configurations.getAt("embed"))
      extendsFrom(configurations.getAt("embedTree"))

      variant.buildType?.let { buildType ->
        extendsFrom(configurations.getAt("${buildType}Embed"))
        extendsFrom(configurations.getAt("${buildType}EmbedTree"))

        // Add build type attribute to match incoming dependencies.
        with(agp) { incoming.attributes.buildTypeAttribute(buildType) }
      }

      setTransitivity(true)
      isCanBeConsumed = false
      isCanBeResolved = true

      // Match incoming dependencies as targeting Android.
      incoming.attributes.attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.ANDROID))
      incoming.attributes.attribute(USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))

      // Incoming dependencies should be mergeable artifacts as per ArtifactTypeCompatibilityDependencyRule.
      incoming.attributes.attribute(ARTIFACT_TYPE_ATTRIBUTE, MERGEABLE_ARTIFACT_TYPE)
    }

    val androidAaptIgnoreEnv = providers.environmentVariable("ANDROID_AAPT_IGNORE").orElse("")

    val packageVariantAar = tasks.register<PackageAarTask>(variant.name("package", "Aar")) {
      embedArtifacts.set(variantEmbedClasspath.incoming.artifacts)

      shadeEnvironment.set(parseShadeEnvironment(aaraar, variant))
      packagingEnvironment.set(parsePackagingEnvironment(variant))
      keepMetaFiles.set(aaraar.keepMetaFiles)
      androidAaptIgnore.set(androidAaptIgnoreEnv)
      packagerFactory.set(aaraar.packagerFactory)
      postProcessorFactories.set(aaraar.postProcessorFactories)
    }

    variant.registerAarTransform(
      packageVariantAar,
      PackageAarTask::inputAar,
      PackageAarTask::outputAar,
    )

    configurations.create(variant.name("merged", "Aar")) {
      setTransitivity(false)
      isCanBeConsumed = true
      isCanBeResolved = true

      outgoing {
        variant.buildType?.let { buildType ->
          // Add build type attribute to tag outgoing artifacts.
          with(agp) { attributes.buildTypeAttribute(buildType) }
        }

        // Tag outgoing artifacts as targeting Android.
        attributes.attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.ANDROID))
        attributes.attribute(USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))

        // Outgoing artifact is a merged aar (which is still considered mergeable!)
        attributes.attribute(ARTIFACT_TYPE_ATTRIBUTE, MERGED_AAR_TYPE)
        artifact(packageVariantAar.flatMap { it.outputAar })
      }
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
                resourceRenames = emptyMap(),
                resourceDeletes = resourceExclusions,
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
                resourceRenames = it.resourceRenames.get(),
                resourceDeletes = it.resourceDeletes.get(),
              ),
            )
          }
        )
      }.toList(),
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

  private fun Configuration.setTransitivity(transitive: Boolean) {
    isTransitive = transitive

    dependencies.whenObjectAdded {
      if (this is ModuleDependency) {
        isTransitive = transitive
      }
    }
  }
}
