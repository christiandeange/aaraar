package sh.christian.aaraar.model.classeditor

import sh.christian.aaraar.model.classeditor.types.voidType

/**
 * Represents a declared method for a particular class.
 */
interface MethodReference : MemberReference {
  /** The [Parameter] arguments that this constructor must be invoked with. */
  val parameters: List<Parameter>

  /** The constant default value returned by this method, if defined on an annotation class. */
  val defaultValue: AnnotationInstance.Value?

  /** The type that is returned by this method, or [voidType] if none is defined. */
  val returnType: ClassReference
}
