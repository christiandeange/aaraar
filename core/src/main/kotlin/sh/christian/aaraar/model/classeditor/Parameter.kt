package sh.christian.aaraar.model.classeditor

import javassist.CtBehavior
import javassist.bytecode.ByteArray.write16bit
import javassist.bytecode.MethodParametersAttribute

/**
 * Represents an argument that is part of a method or constructor signature.
 *
 * This representation is mutable, to allow changing properties of the parameter.
 */
class Parameter
internal constructor(
  internal val classpath: Classpath,
  internal val behavior: CtBehavior,
  internal val index: Int,
) {
  private val methodParameters: MethodParametersAttribute
    get() = behavior.methodInfo.getAttribute(MethodParametersAttribute.tag) as MethodParametersAttribute

  /** The set of annotations applied to this parameter definition. */
  var annotations: List<AnnotationInstance> by ::parameterAnnotations

  /** The parameter name. */
  var name: String
    get() = methodParameters.parameterName(index)
    set(value) {
      val newIndex = behavior.methodInfo.constPool.addUtf8Info(value)
      write16bit(newIndex, methodParameters.get(), index * INDEX_SIZE + 1)
    }

  /** The type that this parameter stores. */
  var type: ClassReference
    get() = classpath[behavior.parameterTypes[index]]
    set(value) {
      val parameterTypes = behavior.parameterTypes
      parameterTypes[index] = value._class
      behavior.setParameterTypes(parameterTypes)
    }

  private companion object {
    const val INDEX_SIZE = 4
  }
}
