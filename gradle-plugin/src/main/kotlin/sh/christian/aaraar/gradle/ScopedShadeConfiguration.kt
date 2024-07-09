package sh.christian.aaraar.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.setProperty
import sh.christian.aaraar.packaging.ShadeConfigurationScope

/**
 * Configures the scopes and rules for shading class files.
 *
 * See each individual shading method for syntax and usage descriptions.
 */
class ScopedShadeConfiguration
internal constructor(
  internal val scopeSelector: ShadeConfigurationScope,
  objects: ObjectFactory,
) {
  /** @see rename */
  val classRenames: MapProperty<String, String> = objects.mapProperty<String, String>().convention(mutableMapOf())

  /** @see delete */
  val classDeletes: SetProperty<String> = objects.setProperty<String>().convention(mutableSetOf())

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
   * Adds the provided package [prefix] to all classes.
   */
  fun addPrefix(prefix: String) {
    classRenames.put("**.**", "$prefix@0")
    classRenames.put("*", "$prefix@0")
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
