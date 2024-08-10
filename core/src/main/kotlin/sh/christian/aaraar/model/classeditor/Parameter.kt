package sh.christian.aaraar.model.classeditor

/**
 * Represents an argument that is part of a method or constructor signature.
 */
interface Parameter {
  /** The set of annotations applied to this parameter definition. */
  val annotations: List<AnnotationInstance>

  /** The parameter name. */
  val name: String

  /** The type that this parameter stores. */
  val type: ClassReference
}
