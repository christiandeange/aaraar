package sh.christian.aaraar.shading.impl.jarjar.transform.config

import java.util.regex.Matcher
import java.util.regex.Pattern

internal abstract class AbstractPattern(patternText: String) {
  val pattern: Pattern = PatternUtils.newPattern(patternText)

  fun getMatcher(value: String): Matcher? {
    return if (PatternUtils.isPossibleQualifiedName(value, "/")) {
      pattern.matcher(value).takeIf { it.matches() }
    } else {
      null
    }
  }

  fun matches(value: String): Boolean {
    return getMatcher(value) != null
  }

  override fun toString(): String {
    return pattern.pattern()
  }
}
