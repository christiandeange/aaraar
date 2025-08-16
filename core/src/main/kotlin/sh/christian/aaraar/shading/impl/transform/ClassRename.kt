package sh.christian.aaraar.shading.impl.transform

internal class ClassRename(
  patternText: String,
  replaceText: String,
) : AbstractClassPattern(patternText), ReplacePattern {
  private val replace: List<ReplacePart> = RegexUtils.newReplace(replaceText, forClass = true)

  override fun replace(value: String): String? {
    return RegexUtils.replace(this, replace, value)
  }
}
