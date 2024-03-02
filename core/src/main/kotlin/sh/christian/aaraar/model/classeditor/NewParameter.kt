package sh.christian.aaraar.model.classeditor

/**
 * Represents the configuration for a new [Parameter] being added to a method or constructor.
 */
data class NewParameter(
  val name: String,
  val type: ClassReference,
  val annotations: List<AnnotationInstance> = emptyList(),
) {
  override fun toString(): String {
    return "$name: $type"
  }
}
