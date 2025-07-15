package sh.christian.aaraar.shading.impl.jarjar.transform.config

internal class ResourceRename(
  patternText: String,
  replaceText: String
) : AbstractResourcePattern(patternText) {
  private val replace: List<ReplacePart> = PatternUtils.newReplace(pattern, replaceText)

  fun replace(value: String): String? {
    return PatternUtils.replace(this, replace, value)
  }
}
