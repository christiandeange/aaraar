package sh.christian.aaraar.model.classeditor

/**
 * Represents a declared field for a particular class.
 */
interface FieldReference : MemberReference {
  /** The type that this field stores. */
  val type: ClassReference
}
