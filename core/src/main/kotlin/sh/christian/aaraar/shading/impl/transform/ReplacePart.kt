package sh.christian.aaraar.shading.impl.transform

internal sealed interface ReplacePart {
  data class Literal(
    val value: String,
  ) : ReplacePart

  data class Group(
    val index: Int,
  ) : ReplacePart
}
