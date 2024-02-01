package sh.christian.aaraar.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import javax.inject.Inject

/**
 * Configures behaviour of [AarAarPlugin]. Accessible via the `aaraar` extension.
 */
@Suppress("UnnecessaryAbstractClass")
abstract class AarAarExtension
@Inject constructor(
  objects: ObjectFactory,
) {
  /** @see rename */
  val classRenames: MapProperty<String, String> = objects.mapProperty<String, String>().convention(mutableMapOf())

  /** @see delete */
  val classDeletes: SetProperty<String> = objects.setProperty<String>().convention(mutableSetOf())

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
   * Renames any classes that match the provided [pattern].
   *
   * The [pattern] will match class names, with optional wildcards:
   * - `*` will match a single package component.
   * - `**` will match against the remainder of any valid fully-qualified class name.
   *
   * [replacement] is a class name which can optionally reference the substrings matched by the wildcards.
   * A numbered reference is available for every wildcard in the pattern, starting from left to right: `@1`, `@2`, etc.
   * A special `@0` reference contains the entire matched class name.
   */
  fun rename(pattern: String, replacement: String) {
    classRenames.put(pattern, replacement)
  }

  /**
   * Deletes any classes that match the provided [pattern].
   *
   * The [pattern] will match class names, with optional wildcards:
   * - `*` will match a single package component.
   * - `**` will match against the remainder of any valid fully-qualified class name.
   */
  fun delete(pattern: String) {
    classDeletes.add(pattern)
  }
}
