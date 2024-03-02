package sh.christian.aaraar.model.classeditor

import javassist.CtBehavior
import javassist.bytecode.AnnotationDefaultAttribute
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.AttributeInfo
import javassist.bytecode.ConstPool
import javassist.bytecode.Descriptor
import javassist.bytecode.ParameterAnnotationsAttribute
import sh.christian.aaraar.model.classeditor.AnnotationInstance.Value.Companion.toValue

internal var ClassReference.classAnnotations: List<AnnotationInstance>
  get() {
    val visible = _class.classFile.getAttribute(AnnotationsAttribute.visibleTag)
    val invisible = _class.classFile.getAttribute(AnnotationsAttribute.invisibleTag)
    return getAnnotations(classpath, visible, true) + getAnnotations(classpath, invisible, false)
  }
  set(value) {
    val (visible, invisible) = value.partition { it.isVisible }

    val classFile = _class.classFile
    classFile.addAttribute(newAnnotation(classFile.constPool, visible, AnnotationsAttribute.visibleTag))
    classFile.addAttribute(newAnnotation(classFile.constPool, invisible, AnnotationsAttribute.invisibleTag))
  }

internal var MethodReference.methodAnnotations: List<AnnotationInstance>
  get() {
    val visible = _method.methodInfo.getAttribute(AnnotationsAttribute.visibleTag)
    val invisible = _method.methodInfo.getAttribute(AnnotationsAttribute.invisibleTag)
    return getAnnotations(classpath, visible, true) + getAnnotations(classpath, invisible, false)
  }
  set(value) {
    val (visible, invisible) = value.partition { it.isVisible }

    val constPool = _method.methodInfo.constPool
    _method.methodInfo.addAttribute(newAnnotation(constPool, visible, AnnotationsAttribute.visibleTag))
    _method.methodInfo.addAttribute(newAnnotation(constPool, invisible, AnnotationsAttribute.invisibleTag))
  }

internal var ConstructorReference.constructorAnnotations: List<AnnotationInstance>
  get() {
    val visible = _constructor.methodInfo.getAttribute(AnnotationsAttribute.visibleTag)
    val invisible = _constructor.methodInfo.getAttribute(AnnotationsAttribute.invisibleTag)
    return getAnnotations(classpath, visible, true) + getAnnotations(classpath, invisible, false)
  }
  set(value) {
    val (visible, invisible) = value.partition { it.isVisible }

    val constPool = _constructor.methodInfo.constPool
    _constructor.methodInfo.addAttribute(newAnnotation(constPool, visible, AnnotationsAttribute.visibleTag))
    _constructor.methodInfo.addAttribute(newAnnotation(constPool, invisible, AnnotationsAttribute.invisibleTag))
  }

internal var FieldReference.fieldAnnotations: List<AnnotationInstance>
  get() {
    val visible = _field.fieldInfo.getAttribute(AnnotationsAttribute.visibleTag)
    val invisible = _field.fieldInfo.getAttribute(AnnotationsAttribute.invisibleTag)
    return getAnnotations(classpath, visible, true) + getAnnotations(classpath, invisible, false)
  }
  set(value) {
    val (visible, invisible) = value.partition { it.isVisible }

    val constPool = _field.fieldInfo.constPool
    _field.fieldInfo.addAttribute(newAnnotation(constPool, visible, AnnotationsAttribute.visibleTag))
    _field.fieldInfo.addAttribute(newAnnotation(constPool, invisible, AnnotationsAttribute.invisibleTag))
  }

internal var Parameter.parameterAnnotations: List<AnnotationInstance>
  get() {
    val visible = behavior.methodInfo.getAttribute(ParameterAnnotationsAttribute.visibleTag)
    val invisible = behavior.methodInfo.getAttribute(ParameterAnnotationsAttribute.invisibleTag)
    return getAnnotations(classpath, visible, true, index) + getAnnotations(classpath, invisible, false, index)
  }
  set(value) {
    val (visible, invisible) = value.partition { it.isVisible }
    behavior.methodInfo.addAttribute(
      newParameterAnnotation(behavior, visible, ParameterAnnotationsAttribute.visibleTag, index)
    )

    behavior.methodInfo.addAttribute(
      newParameterAnnotation(behavior, invisible, ParameterAnnotationsAttribute.invisibleTag, index)
    )
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
  visible: Boolean,
): List<AnnotationInstance> {
  return (attributeInfo as AnnotationsAttribute?)?.annotations
    ?.map { classpath[it, visible] }
    .orEmpty()
}

internal fun getAnnotations(
  classpath: Classpath,
  attributeInfo: AttributeInfo?,
  visible: Boolean,
  index: Int,
): List<AnnotationInstance> {
  return (attributeInfo as ParameterAnnotationsAttribute?)?.annotations
    ?.get(index)
    ?.map { classpath[it, visible] }
    .orEmpty()
}

internal fun newAnnotation(
  constPool: ConstPool,
  annotations: List<AnnotationInstance>,
  tag: String,
): AnnotationsAttribute {
  val attribute = AnnotationsAttribute(constPool, tag)
  attribute.annotations = annotations.mapToArray { it._annotation }
  return attribute
}

internal fun newParameterAnnotation(
  behavior: CtBehavior,
  annotations: List<AnnotationInstance>,
  tag: String,
  index: Int,
): ParameterAnnotationsAttribute {
  val existingAttribute = behavior.methodInfo.getAttribute(tag) as ParameterAnnotationsAttribute?

  val (attribute, allAnnotations) = if (existingAttribute != null) {
    existingAttribute to existingAttribute.annotations
  } else {
    val newAttribute = ParameterAnnotationsAttribute(behavior.methodInfo.constPool, tag)
    newAttribute to Array(Descriptor.numOfParameters(behavior.methodInfo.descriptor)) { emptyArray() }
  }

  allAnnotations[index] = annotations.mapToArray { it._annotation }
  attribute.annotations = allAnnotations
  return attribute
}
