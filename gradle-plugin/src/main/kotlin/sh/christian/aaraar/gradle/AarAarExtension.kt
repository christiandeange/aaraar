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
abstract class AarAarExtension
@Inject constructor(
  objects: ObjectFactory,
) {
  /** @see rename */
  val classRenames: MapProperty<String, String> = objects.mapProperty<String, String>().convention(mutableMapOf())

  /** @see delete */
  val classDeletes: SetProperty<String> = objects.setProperty<String>().convention(mutableSetOf())

  /**
   * Dictates whether `META-INF/` files should be kept in the `classes.jar` contained in the final merged `.aar` file.
   *
   * Defaults to `false`.
   */
  val keepMetaFiles: Property<Boolean> = objects.property<Boolean>().convention(false)

  /**
   * Renames any classes that match the provided [pattern].
   *
   * The [pattern] must match a class name with optional wildcards.
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
   * The [pattern] must match a class name with optional wildcards.
   * - `*` will match a single package component.
   * - `**` will match against the remainder of any valid fully-qualified class name.
   */
  fun delete(pattern: String) {
    classDeletes.add(pattern)
  }
}
