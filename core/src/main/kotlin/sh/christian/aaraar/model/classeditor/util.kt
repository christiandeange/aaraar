package sh.christian.aaraar.model.classeditor

import javassist.CtBehavior
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtMethod
import javassist.bytecode.Descriptor
import javassist.bytecode.MethodParametersAttribute
import javassist.bytecode.ParameterAnnotationsAttribute

internal fun CtBehavior.setParameters(parameters: List<NewParameter>) {
  val constPool = methodInfo.constPool
  val parameterTypes = parameters.map { it.type._class }.toTypedArray()
  val parameterNames = parameters.map { it.name }.toTypedArray()
  val parameterAnnotations =
    parameters.map { it.annotations.map { a -> a._annotation }.toTypedArray() }.toTypedArray()

  setParameterTypes(parameterTypes)

  if (parameters.isEmpty()) {
    methodInfo.removeAttribute(MethodParametersAttribute.tag)
  } else {
    val namesAttribute = MethodParametersAttribute(constPool, parameterNames, IntArray(parameters.size) { 0 })
    methodInfo.addAttribute(namesAttribute)
  }

  if (parameterAnnotations.flatten().isEmpty()) {
    methodInfo.removeAttribute(ParameterAnnotationsAttribute.visibleTag)
  } else {
    val annotationsAttribute = ParameterAnnotationsAttribute(constPool, ParameterAnnotationsAttribute.visibleTag)
    annotationsAttribute.annotations = parameterAnnotations
    methodInfo.addAttribute(annotationsAttribute)
  }
}

internal fun CtBehavior.setParameterTypes(parameterTypes: Array<CtClass>) {
  methodInfo.descriptor = when (this) {
    is CtMethod -> Descriptor.ofMethod(this.returnType, parameterTypes)
    is CtConstructor -> Descriptor.ofConstructor(parameterTypes)
    else -> error("Unknown behaviour: ${this::class}")
  }
}
