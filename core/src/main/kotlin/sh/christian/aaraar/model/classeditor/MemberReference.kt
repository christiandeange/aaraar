package sh.christian.aaraar.model.classeditor

/**
 * Represents a declared member (ie: constructor, field, or method) for a particular class.
 */
interface MemberReference {
  /** The member name. */
  val name: String

  /** The set of annotations applied to this member definition. */
  val annotations: List<AnnotationInstance>

  /** The set of modifiers applied to the member definition. */
  val modifiers: Set<Modifier>
}
