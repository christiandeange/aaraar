package sh.christian.aaraar.shading.impl.transform

internal abstract class AbstractPattern {
  protected abstract val regex: Regex

  open fun matchOrNull(value: String): MatchResult? {
    return regex.matchEntire(value)
  }

  fun matches(value: String): Boolean {
    return regex.matches(value)
  }

  override fun toString(): String {
    return regex.pattern
  }
}
