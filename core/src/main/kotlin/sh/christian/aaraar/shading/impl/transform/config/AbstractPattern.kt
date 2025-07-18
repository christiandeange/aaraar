package sh.christian.aaraar.shading.impl.transform.config

internal abstract class AbstractPattern(patternText: String) {
  private val regex: Regex = RegexUtils.newPattern(patternText)

  fun matchOrNull(value: String): MatchResult? {
    return if (RegexUtils.isPossibleQualifiedName(value, "/")) {
      regex.matchEntire(value)
    } else {
      null
    }
  }

  fun matches(value: String): Boolean {
    return regex.matches(value)
  }

  override fun toString(): String {
    return regex.pattern
  }
}
