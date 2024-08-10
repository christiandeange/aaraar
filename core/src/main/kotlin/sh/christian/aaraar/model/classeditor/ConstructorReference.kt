package sh.christian.aaraar.model.classeditor

/**
 * Represents a declared constructor for a particular class.
 */
interface ConstructorReference : MemberReference {
  /** The [Parameter] arguments that this constructor must be invoked with. */
  val parameters: List<Parameter>

  /** The type that is instantiated by this constructor. */
  val type: ClassReference
}
