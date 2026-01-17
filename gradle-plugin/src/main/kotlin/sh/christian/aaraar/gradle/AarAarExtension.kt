package sh.christian.aaraar.gradle

import org.gradle.api.Action
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import sh.christian.aaraar.packaging.ArtifactArchiveProcessor
import sh.christian.aaraar.packaging.DefaultPackager
import sh.christian.aaraar.packaging.Packager
import sh.christian.aaraar.packaging.artifactArchiveProcessorFromClassName
import javax.inject.Inject

/**
 * Configures behaviour of [AarAarPlugin]. Accessible via the `aaraar` extension.
 */
@Suppress("UnnecessaryAbstractClass")
abstract class AarAarExtension
@Inject constructor(
  objects: ObjectFactory,
  dependencies: DependencyHandler,
) {
  /** @see shading */
  val shading = AarAarShading(objects, dependencies)

  /**
   * Dictates whether `META-INF/` files should be kept or discarded in the final merged artifact.
   *
   * Defaults to `true`.
   */
  val keepMetaFiles: Property<Boolean> = objects.property<Boolean>().convention(true)

  /** @see isEnabledForVariant */
  val variantFilter: Property<(VariantDescriptor) -> Boolean> =
    objects.property<(VariantDescriptor) -> Boolean>().convention { true }

  /**
   * Sets the factory to create the [Packager] responsible for merging and processing archives.
   *
   * Defaults to [DefaultPackager.Factory].
   */
  val packagerFactory: Property<Packager.Factory> =
    objects.property<Packager.Factory>().convention(DefaultPackager.Factory())

  /** @see addPostProcessorFactory */
  val postProcessorFactories: ListProperty<ArtifactArchiveProcessor.Factory> =
    objects.listProperty<ArtifactArchiveProcessor.Factory>().convention(emptyList())

  /**
   * Configure the rules for shading class files.
   */
  fun shading(configure: Action<in AarAarShading>) {
    configure(shading)
  }

  /**
   * Dictates whether aaraar packaging should be applied to a given Android variant.
   * This filter is ignored when applied to a non-Android module.
   *
   * Defaults to `true` for all variants.
   * Recommended to only be applied to variant(s) you intend to publish.
   */
  fun isEnabledForVariant(filter: (VariantDescriptor) -> Boolean) {
    variantFilter.set(filter)
  }

  /**
   * Establish a factory to create an [ArtifactArchiveProcessor], which will allow for additional post-processing on the
   * resulting archive file after it has been fully merged.
   *
   * If this is being set via a class name, the factory class must have a public no-arg constructor.
   */
  fun addPostProcessorFactory(className: String) {
    postProcessorFactories.add(artifactArchiveProcessorFromClassName(className))
  }

  /**
   * Establish a factory to create an [ArtifactArchiveProcessor], which will allow for additional post-processing on the
   * resulting archive file after it has been fully merged.
   */
  fun addPostProcessorFactory(factory: ArtifactArchiveProcessor.Factory) {
    postProcessorFactories.add(factory)
  }
}
