package sh.christian.aaraar.model.classeditor

/**
 * Represents a declared field for a particular class.
 */
interface FieldReference : MemberReference {
  /** The JVM field signature. */
  val signature: Signature

  /** The type that this field stores. */
  val type: ClassReference
}
