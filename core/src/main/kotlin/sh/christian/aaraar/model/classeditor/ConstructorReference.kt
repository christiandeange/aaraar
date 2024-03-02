package sh.christian.aaraar.model.classeditor

import javassist.CtConstructor
import javassist.bytecode.Descriptor

/**
 * Represents a declared constructor for a particular class.
 *
 * This representation is mutable, to allow changing properties of the constructor.
 */
class ConstructorReference
internal constructor(
  internal val classpath: Classpath,
  internal val _constructor: CtConstructor,
) : MemberReference(_constructor) {
  override val name: String by _constructor::name

  /** The type that is instantiated by this constructor. */
  val type: ClassReference get() = classpath[_constructor.declaringClass]

  override var annotations: List<AnnotationInstance> by ::constructorAnnotations

  /**
   * The [Parameter] arguments that this constructor must be invoked with.
   *
   * Parameter instances in this list are mutable to support updating each parameter's individual properties.
   * Call [setParameters] to update the list of parameters, or [setParameter] to replace an individual one.
   */
  val parameters: List<Parameter>
    get() {
      val parameterCount = Descriptor.numOfParameters(_constructor.methodInfo.descriptor)
      return List(parameterCount) { index ->
        Parameter(classpath, _constructor, index)
      }
    }

  /** Updates the list of all parameter arguments that this constructor must be invoked with. */
  fun setParameters(vararg parameters: NewParameter) = setParameters(parameters.toList())

  /** Updates the list of all parameter arguments that this constructor must be invoked with. */
  fun setParameters(parameters: List<NewParameter>) {
    _constructor.setParameters(parameters)
  }

  /** Replaces an individual parameter argument. */
  fun setParameter(
    index: Int,
    parameter: NewParameter,
  ) {
    Parameter(classpath, _constructor, index).apply {
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
    if (other !is ConstructorReference) return false
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
    val classname = _constructor.declaringClass.name
    val parameterStrings = parameters.joinToString(", ")
    return "$classname($parameterStrings)"
  }
}
