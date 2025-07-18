package sh.christian.aaraar.shading.impl.transform.config

internal class ClassRename(
  patternText: String,
  replaceText: String,
) : AbstractClassPattern(patternText) {
  private val replace: List<ReplacePart> = RegexUtils.newReplace(replaceText)

  fun replace(value: String): String? {
    return RegexUtils.replace(this, replace, value)
  }
}
