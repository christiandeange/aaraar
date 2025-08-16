package sh.christian.aaraar.shading.impl.transform

internal abstract class AbstractClassPattern(patternText: String) : AbstractPattern() {
  override val regex: Regex = RegexUtils.newPattern(check(patternText), forClass = true)

  override fun matchOrNull(value: String): MatchResult? {
    return if (RegexUtils.isPossibleQualifiedName(value, "/")) {
      super.matchOrNull(value)
    } else {
      null
    }
  }

  private companion object {
    fun check(patternText: String): String {
      require('/' !in patternText) {
        "Class patterns cannot contain slashes"
      }
      return patternText.replace('.', '/')
    }
  }
}
