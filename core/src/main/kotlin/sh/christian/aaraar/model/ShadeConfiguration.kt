package sh.christian.aaraar.model

import java.io.Serializable

/**
 * Defines the rules for shading, following a syntax for pattern matching classes and resources.
 *
 * The pattern will be matched against class names or resource paths, allowing for wildcards to be specified:
 * - `*` will match a single package component or path part.
 * - `**` will match against the remainder of any valid fully-qualified class name or resource path.
 *
 * For renamed classes and resources, the replacement string can reference the substrings matched by the wildcards.
 * A numbered reference is available for every wildcard in the pattern, starting from left to right: `@1`, `@2`, etc.
 * A special `@0` reference contains the entire matched class name or resource path.
 */
data class ShadeConfiguration(
  val classRenames: Map<String, String>,
  val classDeletes: Set<String>,
  val resourceRenames: Map<String, String>,
  val resourceDeletes: Set<String>,
) : Serializable {

  /**
   * Returns `true` if there are no rules specified, or `false` otherwise.
   */
  fun isEmpty(): Boolean {
    return classRenames.isEmpty() && classDeletes.isEmpty() && resourceRenames.isEmpty() && resourceDeletes.isEmpty()
  }

  companion object {
    private const val serialVersionUID = 1L
  }
}
