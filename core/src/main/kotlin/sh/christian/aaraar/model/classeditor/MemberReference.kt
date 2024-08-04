package sh.christian.aaraar.model.classeditor

import javassist.CtMember
import sh.christian.aaraar.model.classeditor.Modifier.Companion.toModifiers

/**
 * Represents a declared member (ie: constructor, field, or method) for a particular class.
 *
 * This representation is mutable, to allow changing properties of the member.
 */
abstract class MemberReference
internal constructor(
  private val _member: CtMember,
) {
  /** The set of modifiers applied to the member definition. */
  var modifiers: Set<Modifier>
    get() = Modifier.fromModifiers(_member.modifiers)
    set(value) {
      _member.modifiers = value.toModifiers()
    }

  /** The member name. */
  abstract val name: String

  /** The set of annotations applied to this member definition. */
  abstract val annotations: List<AnnotationInstance>
}
