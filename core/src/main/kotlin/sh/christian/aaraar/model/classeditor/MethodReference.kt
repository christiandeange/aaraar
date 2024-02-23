package sh.christian.aaraar.model.classeditor

import javassist.CtMethod
import javassist.bytecode.Descriptor
import javassist.bytecode.Descriptor.changeReturnType
import sh.christian.aaraar.model.classeditor.types.voidType

/**
 * Represents a declared method for a particular class.
 *
 * This representation is mutable, to allow changing properties of the method.
 */
class MethodReference
internal constructor(
  internal val classpath: Classpath,
  internal val _method: CtMethod,
) : MemberReference(_method) {
  override var name: String by _method::name

  override var annotations: List<AnnotationInstance> by ::methodAnnotations

  /** The type that is returned by this method, or [voidType] if none is defined. */
  var returnType: ClassReference
    get() = classpath[_method.returnType]
    set(value) {
      _method.methodInfo.descriptor = changeReturnType(value.qualifiedName, _method.methodInfo.descriptor)
    }

  /** The constant default value returned by this method, if defined on an annotation class. */
  var defaultValue: AnnotationInstance.Value? by ::annotationDefaultValue

  /**
   * The [Parameter] arguments that this constructor must be invoked with.
   *
   * Parameter instances in this list are mutable to support updating each parameter's individual properties.
   * Call [setParameters] to update the list of parameters, or [setParameter] to replace an individual one.
   */
  val parameters: List<Parameter>
    get() {
      val parameterCount = Descriptor.numOfParameters(_method.methodInfo.descriptor)
      return List(parameterCount) { index ->
        Parameter(classpath, _method, index)
      }
    }

  /** Updates the list of all parameter arguments that this constructor must be invoked with. */
  fun setParameters(vararg parameters: NewParameter) = setParameters(parameters.toList())

  /** Updates the list of all parameter arguments that this constructor must be invoked with. */
  fun setParameters(parameters: List<NewParameter>) {
    _method.setParameters(parameters)
  }

  /** Replaces an individual parameter argument. */
  fun setParameter(
    index: Int,
    parameter: NewParameter,
  ) {
    Parameter(classpath, _method, index).apply {
      annotations = parameter.annotations
      name = parameter.name
      type = parameter.type
    }
  }

  /** Removes bytecode stored to describe this method's implementation. */
  fun removeMethodBody() {
    _method.methodInfo.removeCodeAttribute()
  }

  override fun toString(): String {
    val count = parameters.count()
    return if (count == 0) {
      "${_method.declaringClass.name}()"
    } else {
      "${_method.declaringClass.name}([$count parameters])"
    }
  }
}
