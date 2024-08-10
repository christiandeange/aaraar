package sh.christian.aaraar.model.classeditor

import javassist.CtBehavior
import javassist.bytecode.ByteArray
import javassist.bytecode.MethodParametersAttribute

/**
 * Represents an argument that is part of a method or constructor signature.
 *
 * This representation is mutable, to allow changing properties of the parameter.
 */
class MutableParameter
internal constructor(
  internal val classpath: MutableClasspath,
  internal val behavior: CtBehavior,
  internal val index: Int,
) : Parameter {
  private val methodParameters: MethodParametersAttribute
    get() = behavior.methodInfo.getAttribute(MethodParametersAttribute.tag) as MethodParametersAttribute

  override var annotations: List<AnnotationInstance> by ::parameterAnnotations

  override var name: String
    get() = methodParameters.parameterName(index)
    set(value) {
      val newIndex = behavior.methodInfo.constPool.addUtf8Info(value)
      ByteArray.write16bit(newIndex, methodParameters.get(), index * INDEX_SIZE + 1)
    }

  override var type: MutableClassReference
    get() = classpath[behavior.parameterTypes[index]]
    set(value) {
      val parameterTypes = behavior.parameterTypes
      parameterTypes[index] = value._class
      behavior.setParameterTypes(parameterTypes)
    }

  override fun equals(other: Any?): Boolean {
    if (other !is MutableParameter) return false
    return behavior == other.behavior && index == other.index
  }

  override fun hashCode(): Int {
    var result = index
    result = 31 * result + annotations.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }

  override fun toString(): String {
    return "$name: $type"
  }

  private companion object {
    const val INDEX_SIZE = 4
  }
}
