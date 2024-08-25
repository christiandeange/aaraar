package sh.christian.aaraar.model.classeditor

import javassist.CtBehavior
import javassist.bytecode.ByteArray
import javassist.bytecode.MethodParametersAttribute
import kotlinx.metadata.KmValueParameter

/**
 * Represents an argument that is part of a method or constructor signature.
 *
 * This representation is mutable, to allow changing properties of the parameter.
 */
class MutableParameter
internal constructor(
  private val owner: ParameterOwner,
  internal val index: Int,
) : Parameter {
  internal val classpath: MutableClasspath = owner.classpath

  internal val behavior: CtBehavior = owner.behavior

  internal val parameterMetadata: KmValueParameter? = when (owner) {
    is FromConstructor -> owner.constructor.constructorMetadata?.valueParameters?.getOrNull(index)
    is FromMethod -> owner.method.functionMetadata?.valueParameters?.getOrNull(index)
  }

  override var annotations: List<AnnotationInstance> by ::parameterAnnotations

  override var name: String
    get() = behavior.get(Attribute.MethodParameters)?.parameterName(index) ?: "p$index"
    set(value) {
      val existingAttribute = behavior.get(Attribute.MethodParameters)
      val methodParameters = if (existingAttribute != null) {
        existingAttribute
      } else {
        val newAttribute = MethodParametersAttribute(
          behavior.methodInfo.constPool,
          Array(behavior.parameterTypes.size) { "p$it" },
          IntArray(behavior.parameterTypes.size),
        )
        behavior.set(Attribute.MethodParameters, newAttribute)
        newAttribute
      }

      val newIndex = behavior.methodInfo.constPool.addUtf8Info(value)
      ByteArray.write16bit(newIndex, methodParameters.get(), index * INDEX_SIZE + 1)

      parameterMetadata?.name = value
    }

  override var type: MutableClassReference
    get() = owner.classpath[behavior.parameterTypes[index]]
    set(value) {
      val parameterTypes = behavior.parameterTypes
      parameterTypes[index] = value._class
      behavior.setParameterTypes(parameterTypes)

      parameterMetadata?.type = classpath.kmType(value.qualifiedName)
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
