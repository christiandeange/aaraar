package sh.christian.aaraar.shading.impl.jarjar.transform.config

internal abstract class AbstractClassPattern(patternText: String) : AbstractPattern(check(patternText)) {
  private companion object {
    fun check(patternText: String): String {
      require('/' !in patternText) {
        "Class patterns cannot contain slashes"
      }
      return patternText.replace('.', '/')
    }
  }
}
