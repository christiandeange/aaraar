package sh.christian.aaraar.model.classeditor

import javassist.CtField
import javassist.bytecode.ConstantAttribute

/**
 * Represents a declared field for a particular class.
 *
 * This representation is mutable, to allow changing properties of the field.
 */
class MutableFieldReference
internal constructor(
  internal val classpath: MutableClasspath,
  internal val _field: CtField,
) : MutableMemberReference(_field), FieldReference {

  override var name: String by _field::name

  override var annotations: List<AnnotationInstance> by ::fieldAnnotations

  override var type: MutableClassReference
    get() = classpath[_field.type]
    set(value) {
      _field.type = value._class
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
