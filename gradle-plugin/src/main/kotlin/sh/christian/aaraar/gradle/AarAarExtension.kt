package sh.christian.aaraar.gradle

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

/**
 * Configures behaviour of [AarAarPlugin]. Accessible via the `aaraar` extension.
 */
@Suppress("UnnecessaryAbstractClass")
abstract class AarAarExtension
@Inject constructor(
  objects: ObjectFactory,
) {
  /** @see shading */
  val shading = AarAarShading(objects)

  /**
   * Dictates whether `META-INF/` files should be kept or discarded in the final merged artifact.
   *
   * Defaults to `true`.
   */
  val keepMetaFiles: Property<Boolean> = objects.property<Boolean>().convention(true)

  /** @see isEnabledForVariant */
  val variantFilter: Property<(VariantDescriptor) -> Boolean> =
    objects.property<(VariantDescriptor) -> Boolean>().convention { true }

  /** @see setApiJarProcessorFactory */
  val apiJarProcessorFactory: Property<ApiJarProcessor.Factory> =
    objects.property<ApiJarProcessor.Factory>().convention(null)

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
   * Establish a factory to create an [ApiJarProcessor], which will enable producing an `api.jar` file in an AAR.
   * This property is ignored when applied to a non-Android module.
   *
   * If this is being set via a class name, the factory class must have a public no-arg constructor.
   */
  fun setApiJarProcessorFactory(classname: String) {
    apiJarProcessorFactory.set(apiJarProcessorFromClassName(classname))
  }

  /**
   * Establish a factory to create an [ApiJarProcessor], which will enable producing an `api.jar` file in an AAR.
   * This property is ignored when applied to a non-Android module.
   */
  fun setApiJarProcessorFactory(factory: ApiJarProcessor.Factory) {
    apiJarProcessorFactory.set(factory)
  }
}
