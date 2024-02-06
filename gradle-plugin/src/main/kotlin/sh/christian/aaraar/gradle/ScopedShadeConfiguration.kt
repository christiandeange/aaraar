package sh.christian.aaraar.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.setProperty

/**
 * Configures the scopes and rules for shading class files.
 *
 * See the [rename] and [delete] methods for syntax and usage descriptions.
 */
class ScopedShadeConfiguration
internal constructor(
  internal val scopeSelector: ScopeSelector,
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

internal sealed interface ScopeSelector {
  object All : ScopeSelector
  class ForGroup(val group: String) : ScopeSelector
  class ForModule(val dependency: Any) : ScopeSelector
  class ForDependency(val dependency: Any) : ScopeSelector
  class ForProject(val path: String) : ScopeSelector
}
