package sh.christian.aaraar.model.classeditor

import javassist.CtMethod
import javassist.bytecode.Descriptor
import sh.christian.aaraar.model.classeditor.Modifier.Companion.toModifiers
import sh.christian.aaraar.model.classeditor.metadata.signature
import sh.christian.aaraar.model.classeditor.metadata.toVisibility
import kotlin.metadata.KmFunction
import kotlin.metadata.KmValueParameter
import kotlin.metadata.hasAnnotations
import kotlin.metadata.visibility

/**
 * Represents a declared method for a particular class.
 *
 * This representation is mutable, to allow changing properties of the method.
 */
class MutableMethodReference
internal constructor(
  internal val classpath: MutableClasspath,
  internal val _method: CtMethod,
) : MutableMemberReference(), MethodReference {
  override val signature: Signature
    get() = MethodSignature(_method.name, _method.methodInfo.descriptor)

  val functionMetadata: KmFunction? =
    classpath[_method.declaringClass].kotlinMetadata?.kmClass?.functions
      ?.firstOrNull { it.signature() == signature }

  override var modifiers: Set<Modifier>
    get() = Modifier.fromModifiers(_method.modifiers)
    set(value) {
      _method.modifiers = value.toModifiers()
      functionMetadata?.visibility = value.toVisibility()
    }

  override var name: String
    get() = _method.name
    set(value) {
      _method.name = value
      functionMetadata?.name = value
    }

  override var annotations: List<AnnotationInstance> by ::methodAnnotations

  override var returnType: MutableClassReference
    get() = classpath[_method.returnType]
    set(value) {
      _method.methodInfo.descriptor = Descriptor.changeReturnType(value.qualifiedName, _method.methodInfo.descriptor)
      functionMetadata?.returnType?.classifier = classpath.kmClassifier(value.qualifiedName)
    }

  override var defaultValue: AnnotationInstance.Value? by ::annotationDefaultValue

  /**
   * The [MutableParameter] arguments that this constructor must be invoked with.
   *
   * Parameter instances in this list are mutable to support updating each parameter's individual properties.
   * Call [setParameters] to update the list of parameters, or [setParameter] to replace an individual one.
   */
  override val parameters: List<MutableParameter>
    get() {
      val parameterCount = Descriptor.numOfParameters(_method.methodInfo.descriptor)
      return List(parameterCount) { index ->
        MutableParameter(FromMethod(this), index)
      }
    }

  /** Updates the list of all parameter arguments that this constructor must be invoked with. */
  fun setParameters(vararg parameters: NewParameter) = setParameters(parameters.toList())

  /** Updates the list of all parameter arguments that this constructor must be invoked with. */
  fun setParameters(parameters: List<NewParameter>) {
    _method.setParameters(parameters)

    functionMetadata?.valueParameters?.clear()
    functionMetadata?.valueParameters?.addAll(
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
    MutableParameter(FromMethod(this), index).apply {
      annotations = parameter.annotations
      name = parameter.name
      type = parameter.type
    }
  }

  /** Removes bytecode stored to describe this method's implementation. */
  fun removeMethodBody() {
    _method.methodInfo.removeCodeAttribute()
  }

  override fun equals(other: Any?): Boolean {
    if (other !is MutableMethodReference) return false
    return _method == other._method
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + annotations.hashCode()
    result = 31 * result + returnType.hashCode()
    result = 31 * result + (defaultValue?.hashCode() ?: 0)
    result = 31 * result + parameters.hashCode()
    return result
  }

  override fun toString(): String {
    return _method.toKotlinLikeString()
  }
}
