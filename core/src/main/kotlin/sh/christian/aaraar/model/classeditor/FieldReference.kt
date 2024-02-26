package sh.christian.aaraar.model.classeditor

import javassist.CtField
import javassist.bytecode.ConstantAttribute

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

  override fun toString(): String {
    return "${_field.declaringClass.name}.$name"
  }
}
