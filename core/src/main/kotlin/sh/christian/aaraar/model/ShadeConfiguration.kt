package sh.christian.aaraar.model

import java.io.Serializable

/**
 * Defines the rules for shading, following a syntax for pattern matching classes and resources.
 *
 * The pattern will be matched against class names, allowing for wildcards to be specified:
 * - `*` will match a single package component.
 * - `**` will match against the remainder of any valid fully-qualified class name.
 *
 * For renamed classes, the replacement string can reference the substrings matched by the wildcards.
 * A numbered reference is available for every wildcard in the pattern, starting from left to right: `@1`, `@2`, etc.
 * A special `@0` reference contains the entire matched class name.
 */
data class ShadeConfiguration(
  val classRenames: Map<String, String>,
  val classDeletes: Set<String>,
  val resourceExclusions: Set<String>,
) : Serializable {

  /**
   * Returns `true` if there are no rules specified, or `false` otherwise.
   */
  fun isEmpty(): Boolean {
    return classRenames.isEmpty() && classDeletes.isEmpty() && resourceExclusions.isEmpty()
  }

  companion object {
    private const val serialVersionUID = 1L
  }
}
