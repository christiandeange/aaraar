package sh.christian.aaraar.model.classeditor

/**
 * Represents a declared member (ie: constructor, field, or method) for a particular class.
 *
 * This representation is mutable, to allow changing properties of the member.
 */
sealed class MutableMemberReference : MemberReference {
  abstract override var annotations: List<AnnotationInstance>

  abstract override var modifiers: Set<Modifier>
}
