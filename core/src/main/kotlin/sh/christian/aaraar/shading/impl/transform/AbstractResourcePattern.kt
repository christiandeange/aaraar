package sh.christian.aaraar.shading.impl.transform

internal abstract class AbstractResourcePattern(patternText: String) : AbstractPattern() {
  override val regex: Regex = RegexUtils.newPattern(patternText, forClass = false)
}
