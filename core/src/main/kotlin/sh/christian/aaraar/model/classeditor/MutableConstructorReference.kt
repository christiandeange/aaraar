package sh.christian.aaraar.model.classeditor

import javassist.CtConstructor
import javassist.bytecode.Descriptor
import sh.christian.aaraar.model.classeditor.Modifier.Companion.toModifiers
import sh.christian.aaraar.model.classeditor.metadata.signature
import sh.christian.aaraar.model.classeditor.metadata.toVisibility
import kotlin.metadata.KmConstructor
import kotlin.metadata.KmValueParameter
import kotlin.metadata.hasAnnotations
import kotlin.metadata.visibility

/**
 * Represents a declared constructor for a particular class.
 *
 * This representation is mutable, to allow changing properties of the constructor.
 */
class MutableConstructorReference
internal constructor(
  internal val classpath: MutableClasspath,
  internal val _constructor: CtConstructor,
) : MutableMemberReference(), ConstructorReference {
  override val signature: Signature
    get() = ConstructorSignature(_constructor.methodInfo.descriptor)

  val constructorMetadata: KmConstructor? =
    classpath[_constructor.declaringClass].kotlinMetadata?.kmClass?.constructors
      ?.firstOrNull { it.signature() == signature }

  override var modifiers: Set<Modifier>
    get() = Modifier.fromModifiers(_constructor.modifiers)
    set(value) {
      _constructor.modifiers = value.toModifiers()
      constructorMetadata?.visibility = value.toVisibility()
    }

  override val name: String by _constructor::name

  override val type: ClassReference get() = classpath[_constructor.declaringClass]

  override var annotations: List<AnnotationInstance> by ::constructorAnnotations

  /**
   * The [MutableParameter] arguments that this constructor must be invoked with.
   *
   * Parameter instances in this list are mutable to support updating each parameter's individual properties.
   * Call [setParameters] to update the list of parameters, or [setParameter] to replace an individual one.
   */
  override val parameters: List<MutableParameter>
    get() {
      val parameterCount = Descriptor.numOfParameters(_constructor.methodInfo.descriptor)
      return List(parameterCount) { index ->
        MutableParameter(FromConstructor(this), index)
      }
    }

  /** Updates the list of all parameter arguments that this constructor must be invoked with. */
  fun setParameters(vararg parameters: NewParameter) = setParameters(parameters.toList())

  /** Updates the list of all parameter arguments that this constructor must be invoked with. */
  fun setParameters(parameters: List<NewParameter>) {
    _constructor.setParameters(parameters)

    constructorMetadata?.valueParameters?.clear()
    constructorMetadata?.valueParameters?.addAll(
      parameters.map {
        KmValueParameter(it.name).apply {
          type = classpath.kmType(it.type.qualifiedName)
          hasAnnotations = it.annotations.isNotEmpty()
        }
      }
    )
  }

  /** Replaces an individual parameter argument. */
  fun setParameter(
    index: Int,
    parameter: NewParameter,
  ) {
    MutableParameter(FromConstructor(this), index).apply {
      annotations = parameter.annotations
      name = parameter.name
      type = parameter.type
    }
  }

  /** Removes bytecode stored to describe this constructor's implementation. */
  fun removeConstructorBody() {
    _constructor.methodInfo.removeCodeAttribute()
  }

  override fun equals(other: Any?): Boolean {
    if (other !is MutableConstructorReference) return false
    return _constructor == other._constructor
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + type.hashCode()
    result = 31 * result + annotations.hashCode()
    result = 31 * result + parameters.hashCode()
    return result
  }

  override fun toString(): String {
    return _constructor.toKotlinLikeString()
  }
}
