package sh.christian.aaraar.shading.impl.jarjar.util

import java.util.regex.Pattern

internal object ClassNameUtils {
  private val ARRAY_FOR_NAME_PATTERN: Pattern = Pattern.compile("\\[L[\\p{javaJavaIdentifierPart}.]+?;")

  const val EXT_CLASS: String = ".class"

  /**
   * Returns true if the given string looks like a Java array name.
   * @param value The name to inspect.
   * @return true if the given string looks like a Java array name.
   */
  fun isArrayForName(value: String): Boolean {
    return ARRAY_FOR_NAME_PATTERN.matcher(value).matches()
  }

  fun isClass(name: String): Boolean {
    return name.endsWith(EXT_CLASS, ignoreCase = true)
  }
}
