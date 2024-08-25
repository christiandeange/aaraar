package sh.christian.aaraar.model.classeditor

import javassist.CtField
import javassist.bytecode.ConstantAttribute
import kotlinx.metadata.KmProperty
import kotlinx.metadata.visibility
import sh.christian.aaraar.model.classeditor.Modifier.Companion.toModifiers
import sh.christian.aaraar.model.classeditor.metadata.fieldSignature
import sh.christian.aaraar.model.classeditor.metadata.toVisibility

/**
 * Represents a declared field for a particular class.
 *
 * This representation is mutable, to allow changing properties of the field.
 */
class MutableFieldReference
internal constructor(
  internal val classpath: MutableClasspath,
  internal val _field: CtField,
) : MutableMemberReference(), FieldReference {
  override val signature: Signature
    get() = FieldSignature(_field.name, _field.fieldInfo.descriptor)

  val propertyMetadata: KmProperty? =
    classpath[_field.declaringClass].kotlinMetadata?.kmClass?.properties
      ?.firstOrNull { it.fieldSignature() == signature }

  override var modifiers: Set<Modifier>
    get() = Modifier.fromModifiers(_field.modifiers)
    set(value) {
      _field.modifiers = value.toModifiers()
      propertyMetadata?.visibility = value.toVisibility()
    }

  override var name: String
    get() = _field.name
    set(value) {
      _field.name = value
      propertyMetadata?.name = value
    }

  override var annotations: List<AnnotationInstance> by ::fieldAnnotations

  override var type: MutableClassReference
    get() = classpath[_field.type]
    set(value) {
      _field.type = value._class
      propertyMetadata?.returnType?.classifier = classpath.kmClassifier(value.qualifiedName)
    }

  /** Removes bytecode stored to describe this field's constant value, if it is a `static final` field. */
  fun removeConstantInitializer() {
    _field.fieldInfo.removeAttribute(ConstantAttribute.tag)
  }

  override fun equals(other: Any?): Boolean {
    if (other !is MutableFieldReference) return false
    return _field == other._field
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + annotations.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }

  override fun toString(): String {
    val valOrVar = if (Modifier.FINAL in modifiers) "val" else "var"
    return "$valOrVar ${_field.declaringClass.name}.$name: $type"
  }
}
