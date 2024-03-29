package sh.christian.aaraar.model.classeditor

import javassist.CtField
import javassist.bytecode.ConstantAttribute
import sh.christian.aaraar.model.classeditor.Modifier.FINAL

/**
 * Represents a declared field for a particular class.
 *
 * This representation is mutable, to allow changing properties of the field.
 */
class FieldReference
internal constructor(
  internal val classpath: Classpath,
  internal val _field: CtField,
) : MemberReference(_field) {

  override var name: String by _field::name

  override var annotations: List<AnnotationInstance> by ::fieldAnnotations

  /** The type that this field stores. */
  var type: ClassReference
    get() = classpath[_field.type]
    set(value) {
      _field.type = value._class
    }

  /** Removes bytecode stored to describe this field's constant value, if it is a `static final` field. */
  fun removeConstantInitializer() {
    _field.fieldInfo.removeAttribute(ConstantAttribute.tag)
  }

  override fun equals(other: Any?): Boolean {
    if (other !is FieldReference) return false
    return _field == other._field
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + annotations.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }

  override fun toString(): String {
    val valOrVar = if (FINAL in modifiers) "val" else "var"
    return "$valOrVar ${_field.declaringClass.name}.$name: $type"
  }
}
