package sh.christian.aaraar.shading.impl.transform

internal class ResourceRename(
  patternText: String,
  replaceText: String
) : AbstractResourcePattern(patternText), ReplacePattern {
  private val replace: List<ReplacePart> = RegexUtils.newReplace(replaceText, forClass = false)

  override fun replace(value: String): String? {
    return RegexUtils.replace(this, replace, value)
  }
}
