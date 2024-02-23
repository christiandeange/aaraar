package sh.christian.aaraar.model.classeditor

import javassist.bytecode.ConstPool
import javassist.bytecode.annotation.Annotation
import javassist.bytecode.annotation.AnnotationMemberValue
import javassist.bytecode.annotation.ArrayMemberValue
import javassist.bytecode.annotation.BooleanMemberValue
import javassist.bytecode.annotation.ByteMemberValue
import javassist.bytecode.annotation.CharMemberValue
import javassist.bytecode.annotation.ClassMemberValue
import javassist.bytecode.annotation.DoubleMemberValue
import javassist.bytecode.annotation.EnumMemberValue
import javassist.bytecode.annotation.FloatMemberValue
import javassist.bytecode.annotation.IntegerMemberValue
import javassist.bytecode.annotation.LongMemberValue
import javassist.bytecode.annotation.MemberValue
import javassist.bytecode.annotation.ShortMemberValue
import javassist.bytecode.annotation.StringMemberValue
import sh.christian.aaraar.model.classeditor.AnnotationInstance.Value.Companion.toValue

/**
 * Represents the usage of an annotation that is applied to a given usage site.
 *
 * Unlike most other models here, annotation usages themselves are not mutable. Each instance is immutable, so modifying
 * behaviour must be done by adding and removing individual [AnnotationInstance]s at the given usage site.
 */
class AnnotationInstance
internal constructor(
  internal val classpath: Classpath,
  internal val _annotation: Annotation,
) {
  /** The name of the annotation type. */
  val name: String = _annotation.typeName

  /** The annotation type. */
  val type: ClassReference = classpath[name]

  /** The set of constant annotation parameter values for this usage. */
  val parameters: Map<String, Value> =
    _annotation.memberNames.orEmpty()
      .associateWith { name -> _annotation.getMemberValue(name).toValue(classpath) }

  /** Creates a new [Builder] based on this usage site. */
  fun toBuilder(): Builder = Builder(name).apply {
    parameters.forEach { (name, value) ->
      addValue(name, value)
    }
  }

  override fun toString(): String {
    val count = parameters.count()
    return if (count == 0) {
      "@${type.simpleName}"
    } else {
      "@${type.simpleName}([$count parameters])"
    }
  }

  /**
   * A constant value used in an annotation usage site. Constant values must be one of the following types:
   * - [String]
   * - [Class]
   * - [Enum]
   * - Any primitive type
   * - Another annotation usage
   * - [Array] wrapping any of the above types
   */
  sealed interface Value {
    class AnnotationValue(
      val value: AnnotationInstance,
    ) : Value

    class ArrayValue(
      val values: List<Value>,
    ) : Value

    class BooleanValue(
      val value: Boolean,
    ) : Value

    class ByteValue(
      val value: Byte,
    ) : Value

    class CharValue(
      val value: Char,
    ) : Value

    class ClassValue(
      val value: ClassReference,
    ) : Value

    class DoubleValue(
      val value: Double,
    ) : Value

    class EnumValue(
      val type: ClassReference,
      val name: String,
    ) : Value

    class FloatValue(
      val value: Float,
    ) : Value

    class IntegerValue(
      val value: Int,
    ) : Value

    class LongValue(
      val value: Long,
    ) : Value

    class ShortValue(
      val value: Short,
    ) : Value

    class StringValue(
      val value: String,
    ) : Value

    fun toMemberValue(constPool: ConstPool): MemberValue = when (this) {
      is AnnotationValue -> AnnotationMemberValue(value._annotation, constPool)
      is ArrayValue -> ArrayMemberValue(constPool).apply {
        value = values.map { it.toMemberValue(constPool) }.toTypedArray()
      }

      is BooleanValue -> BooleanMemberValue(value, constPool)
      is ByteValue -> ByteMemberValue(value, constPool)
      is CharValue -> CharMemberValue(value, constPool)
      is ClassValue -> ClassMemberValue(value.qualifiedName, constPool)
      is DoubleValue -> DoubleMemberValue(value, constPool)
      is EnumValue -> EnumMemberValue(constPool).apply {
        type = this@Value.type.qualifiedName
        value = this@Value.name
      }

      is FloatValue -> FloatMemberValue(value, constPool)
      is IntegerValue -> IntegerMemberValue(value, constPool)
      is LongValue -> LongMemberValue(value, constPool)
      is ShortValue -> ShortMemberValue(value, constPool)
      is StringValue -> StringMemberValue(value, constPool)
    }

    companion object {
      internal fun MemberValue.toValue(classpath: Classpath): Value = when (this) {
        is AnnotationMemberValue -> AnnotationValue(classpath[value])
        is ArrayMemberValue -> ArrayValue(value.map { it.toValue(classpath) })
        is BooleanMemberValue -> BooleanValue(value)
        is ByteMemberValue -> ByteValue(value)
        is CharMemberValue -> CharValue(value)
        is ClassMemberValue -> ClassValue(classpath[value])
        is DoubleMemberValue -> DoubleValue(value)
        is EnumMemberValue -> EnumValue(classpath[type], value)
        is FloatMemberValue -> FloatValue(value)
        is IntegerMemberValue -> IntegerValue(value)
        is LongMemberValue -> LongValue(value)
        is ShortMemberValue -> ShortValue(value)
        is StringMemberValue -> StringValue(value)
        else -> error("Unknown annotation value type ${this::class}")
      }
    }
  }

  /**
   * Builder class to define a new annotation usage.
   */
  class Builder(private var name: String) {
    constructor(classReference: ClassReference) : this(classReference.qualifiedName)

    private val values = mutableMapOf<String, Value>()

    fun name(name: String) = apply {
      this.name = name
    }

    fun addValue(
      name: String,
      value: Value,
    ) = apply {
      values[name] = value
    }

    fun setValues(values: Map<String, Value>) = apply {
      this.values.clear()
      this.values.putAll(values)
    }

    fun forUseIn(classReference: ClassReference): AnnotationInstance {
      val constPool = classReference._class.classFile.constPool
      val annotation = Annotation(name, constPool).apply {
        values.forEach { (name, value) ->
          addMemberValue(name, value.toMemberValue(constPool))
        }
      }
      return AnnotationInstance(classReference.classpath, annotation)
    }
  }
}
