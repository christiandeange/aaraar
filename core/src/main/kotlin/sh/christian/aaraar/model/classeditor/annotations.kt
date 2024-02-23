package sh.christian.aaraar.model.classeditor

import javassist.bytecode.AnnotationDefaultAttribute
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.AttributeInfo
import javassist.bytecode.Descriptor
import javassist.bytecode.ParameterAnnotationsAttribute
import sh.christian.aaraar.model.classeditor.AnnotationInstance.Value.Companion.toValue

internal var ClassReference.classAnnotations: List<AnnotationInstance>
  get() = getAnnotations(classpath, _class.classFile.getAttribute(AnnotationsAttribute.visibleTag))
  set(value) {
    val newAttribute = AnnotationsAttribute(_class.classFile.constPool, AnnotationsAttribute.visibleTag)
    newAttribute.annotations = value.map { it._annotation }.toTypedArray()
    _class.classFile.addAttribute(newAttribute)
  }

internal var MethodReference.methodAnnotations: List<AnnotationInstance>
  get() = getAnnotations(classpath, _method.methodInfo.getAttribute(AnnotationsAttribute.visibleTag))
  set(value) {
    val newAttribute = AnnotationsAttribute(_method.methodInfo.constPool, AnnotationsAttribute.visibleTag)
    newAttribute.annotations = value.map { it._annotation }.toTypedArray()
    _method.methodInfo.addAttribute(newAttribute)
  }

internal var ConstructorReference.constructorAnnotations: List<AnnotationInstance>
  get() = getAnnotations(classpath, _constructor.methodInfo.getAttribute(AnnotationsAttribute.visibleTag))
  set(value) {
    val newAttribute = AnnotationsAttribute(_constructor.methodInfo.constPool, AnnotationsAttribute.visibleTag)
    newAttribute.annotations = value.map { it._annotation }.toTypedArray()
    _constructor.methodInfo.addAttribute(newAttribute)
  }

internal var FieldReference.fieldAnnotations: List<AnnotationInstance>
  get() = getAnnotations(classpath, _field.fieldInfo.getAttribute(AnnotationsAttribute.visibleTag))
  set(value) {
    val newAttribute = AnnotationsAttribute(_field.fieldInfo.constPool, AnnotationsAttribute.visibleTag)
    newAttribute.annotations = value.map { it._annotation }.toTypedArray()
    _field.fieldInfo.addAttribute(newAttribute)
  }

internal var Parameter.parameterAnnotations: List<AnnotationInstance>
  get() = getParameterAnnotations(
    classpath = classpath,
    attributeInfo = behavior.methodInfo.getAttribute(ParameterAnnotationsAttribute.visibleTag),
    index = index,
  )
  set(value) {
    val existingAttribute =
      behavior.methodInfo.getAttribute(ParameterAnnotationsAttribute.visibleTag) as ParameterAnnotationsAttribute?

    val (attribute, allAnnotations) = if (existingAttribute != null) {
      existingAttribute to existingAttribute.annotations
    } else {
      val newAttribute =
        ParameterAnnotationsAttribute(behavior.methodInfo.constPool, ParameterAnnotationsAttribute.visibleTag)
      newAttribute to Array(Descriptor.numOfParameters(behavior.methodInfo.descriptor)) { emptyArray() }
    }

    allAnnotations[index] = value.map { it._annotation }.toTypedArray()
    attribute.annotations = allAnnotations
    behavior.methodInfo.addAttribute(attribute)
  }

internal var MethodReference.annotationDefaultValue: AnnotationInstance.Value?
  get() {
    return (_method.methodInfo.getAttribute(AnnotationDefaultAttribute.tag) as AnnotationDefaultAttribute?)
      ?.defaultValue?.toValue(classpath)
  }
  set(value) {
    if (value == null) {
      _method.methodInfo.removeAttribute(AnnotationDefaultAttribute.tag)
    } else {
      val newAttribute = AnnotationDefaultAttribute(_method.methodInfo.constPool)
      newAttribute.defaultValue = value.toMemberValue(_method.methodInfo.constPool)
      _method.methodInfo.addAttribute(newAttribute)
    }
  }

internal fun getAnnotations(
  classpath: Classpath,
  attributeInfo: AttributeInfo?,
): List<AnnotationInstance> {
  return (attributeInfo as AnnotationsAttribute?)?.annotations?.map { classpath[it] }.orEmpty()
}

internal fun getParameterAnnotations(
  classpath: Classpath,
  attributeInfo: AttributeInfo?,
  index: Int,
): List<AnnotationInstance> {
  return (attributeInfo as ParameterAnnotationsAttribute?)?.annotations?.get(index)?.map { classpath[it] }.orEmpty()
}
