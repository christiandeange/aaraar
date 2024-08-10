@file:Suppress("UNCHECKED_CAST")

package sh.christian.aaraar.model.classeditor

import javassist.CtBehavior
import javassist.CtClass
import javassist.CtField
import javassist.bytecode.AnnotationDefaultAttribute
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.AttributeInfo
import javassist.bytecode.MethodParametersAttribute
import javassist.bytecode.ParameterAnnotationsAttribute

internal sealed class Attribute<T : AttributeInfo>(val key: String) {
  object VisibleAnnotations :
    Attribute<AnnotationsAttribute>(AnnotationsAttribute.visibleTag)

  object InvisibleAnnotations :
    Attribute<AnnotationsAttribute>(AnnotationsAttribute.invisibleTag)

  object VisibleParameterAnnotations :
    Attribute<ParameterAnnotationsAttribute>(ParameterAnnotationsAttribute.visibleTag)

  object InvisibleParameterAnnotations :
    Attribute<ParameterAnnotationsAttribute>(ParameterAnnotationsAttribute.invisibleTag)

  object AnnotationDefaultValue :
    Attribute<AnnotationDefaultAttribute>(AnnotationDefaultAttribute.tag)

  object MethodParameters :
    Attribute<MethodParametersAttribute>(MethodParametersAttribute.tag)
}

internal fun <T : AttributeInfo> CtClass.get(attribute: Attribute<T>): T? {
  return classFile.getAttribute(attribute.key) as T?
}

internal fun <T : AttributeInfo> CtClass.set(
  attribute: Attribute<T>,
  value: T?,
) {
  if (value == null) classFile.removeAttribute(attribute.key) else classFile.addAttribute(value)
}

internal fun <T : AttributeInfo> CtBehavior.get(attribute: Attribute<T>): T? {
  return methodInfo.getAttribute(attribute.key) as T?
}

internal fun <T : AttributeInfo> CtBehavior.set(
  attribute: Attribute<T>,
  value: T?,
) {
  if (value == null) methodInfo.removeAttribute(attribute.key) else methodInfo.addAttribute(value)
}

internal fun <T : AttributeInfo> CtField.get(attribute: Attribute<T>): T? {
  return fieldInfo.getAttribute(attribute.key) as T?
}

internal fun <T : AttributeInfo> CtField.set(
  attribute: Attribute<T>,
  value: T?,
) {
  if (value == null) fieldInfo.removeAttribute(attribute.key) else fieldInfo.addAttribute(value)
}
