package sh.christian.aaraar.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.setProperty
import sh.christian.aaraar.packaging.ShadeConfigurationScope

/**
 * Configures the scopes and rules for shading classes and resource files.
 *
 * See each individual shading method for syntax and usage descriptions.
 */
class ScopedShadeConfiguration
internal constructor(
  internal val scopeSelector: ShadeConfigurationScope,
  objects: ObjectFactory,
) {
  /** @see renameClass */
  val classRenames: MapProperty<String, String> = objects.mapProperty<String, String>().convention(mutableMapOf())

  /** @see deleteClass */
  val classDeletes: SetProperty<String> = objects.setProperty<String>().convention(mutableSetOf())

  /** @see renameClass */
  val resourceRenames: MapProperty<String, String> = objects.mapProperty<String, String>().convention(mutableMapOf())

  /** @see deleteClass */
  val resourceDeletes: SetProperty<String> = objects.setProperty<String>().convention(mutableSetOf())

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
  fun renameClass(pattern: String, replacement: String) {
    classRenames.put(pattern, replacement)
  }

  /**
   * Deletes any classes that match the provided [pattern].
   *
   * The [pattern] will match class names, with optional wildcards:
   * - `*` will match a single package component.
   * - `**` will match against the remainder of any valid fully-qualified class name.
   */
  fun deleteClass(pattern: String) {
    classDeletes.add(pattern)
  }

  /**
   * Renames any resources that match the provided [pattern].
   *
   * The [pattern] will match resource paths, with optional wildcards:
   * - `*` will match a single path part.
   * - `**` will match against the remainder of any valid fully-qualified resource path.
   *
   * [replacement] is a resource path which can optionally reference the substrings matched by the wildcards.
   * A numbered reference is available for every wildcard in the pattern, starting from left to right: `@1`, `@2`, etc.
   * A special `@0` reference contains the entire matched resource path.
   */
  fun renameResource(pattern: String, replacement: String) {
    resourceRenames.put(pattern, replacement)
  }

  /**
   * Deletes any resources that match the provided [pattern].
   *
   * The [pattern] will match resource paths, with optional wildcards:
   * - `*` will match a single path part.
   * - `**` will match against the remainder of any valid fully-qualified resource path.
   */
  fun deleteResource(pattern: String) {
    resourceDeletes.add(pattern)
  }

  /**
   * Renames any classes or resources that match the provided [pattern].
   *
   * The [pattern] will match class names resource paths, with optional wildcards:
   * - `*` will match a single package component or path part.
   * - `**` will match against the remainder of any valid fully-qualified class name or resource path.
   *
   * [replacement] is a class or resource path which can optionally reference the substrings matched by the wildcards.
   * A numbered reference is available for every wildcard in the pattern, starting from left to right: `@1`, `@2`, etc.
   * A special `@0` reference contains the entire matched class name or resource path.
   */
  fun rename(pattern: String, replacement: String) {
    // The replacement syntax is expected to be in class name format, so convert dots to slashes for resource paths.
    renameClass(pattern, replacement)
    renameResource(pattern, replacement.replace('.', '/'))
  }

  /**
   * Deletes any classes or resources that match the provided [pattern].
   *
   * The [pattern] will match class names or resource paths, with optional wildcards:
   * - `*` will match a single package component or path part.
   * - `**` will match against the remainder of any valid fully-qualified class name or resource path.
   */
  fun delete(pattern: String) {
    deleteClass(pattern)
    deleteResource(pattern)
  }
}
