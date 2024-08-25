package sh.christian.aaraar.model.classeditor

import javassist.CtBehavior
import javassist.bytecode.AnnotationDefaultAttribute
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.AttributeInfo
import javassist.bytecode.ConstPool
import javassist.bytecode.Descriptor
import javassist.bytecode.ParameterAnnotationsAttribute
import kotlinx.metadata.hasAnnotations
import sh.christian.aaraar.model.classeditor.AnnotationInstance.Value.Companion.toValue

internal var MutableClassReference.classAnnotations: List<AnnotationInstance>
  get() {
    val visible = _class.get(Attribute.VisibleAnnotations)
    val invisible = _class.get(Attribute.InvisibleAnnotations)
    return getAnnotations(classpath, visible, true) + getAnnotations(classpath, invisible, false)
  }
  set(value) {
    val (visible, invisible) = value.partition { it.isVisible }

    val constPool = _class.classFile.constPool
    _class.set(
      Attribute.VisibleAnnotations,
      newAnnotation(constPool, visible, Attribute.VisibleAnnotations)
    )
    _class.set(
      Attribute.InvisibleAnnotations,
      newAnnotation(constPool, invisible, Attribute.InvisibleAnnotations)
    )

    kotlinMetadata?.kmClass?.hasAnnotations = value.isNotEmpty()
  }

internal var MutableMethodReference.methodAnnotations: List<AnnotationInstance>
  get() {
    val visible = _method.get(Attribute.VisibleAnnotations)
    val invisible = _method.get(Attribute.InvisibleAnnotations)
    return getAnnotations(classpath, visible, true) + getAnnotations(classpath, invisible, false)
  }
  set(value) {
    val (visible, invisible) = value.partition { it.isVisible }

    val constPool = _method.methodInfo.constPool
    _method.set(
      Attribute.VisibleAnnotations,
      newAnnotation(constPool, visible, Attribute.VisibleAnnotations),
    )
    _method.set(
      Attribute.InvisibleAnnotations,
      newAnnotation(constPool, invisible, Attribute.InvisibleAnnotations),
    )

    functionMetadata?.hasAnnotations = value.isNotEmpty()
  }

internal var MutableConstructorReference.constructorAnnotations: List<AnnotationInstance>
  get() {
    val visible = _constructor.get(Attribute.VisibleAnnotations)
    val invisible = _constructor.get(Attribute.InvisibleAnnotations)
    return getAnnotations(classpath, visible, true) + getAnnotations(classpath, invisible, false)
  }
  set(value) {
    val (visible, invisible) = value.partition { it.isVisible }

    val constPool = _constructor.methodInfo.constPool
    _constructor.set(
      Attribute.VisibleAnnotations,
      newAnnotation(constPool, visible, Attribute.VisibleAnnotations),
    )
    _constructor.set(
      Attribute.InvisibleAnnotations,
      newAnnotation(constPool, invisible, Attribute.InvisibleAnnotations),
    )

    constructorMetadata?.hasAnnotations = value.isNotEmpty()
  }

internal var MutableFieldReference.fieldAnnotations: List<AnnotationInstance>
  get() {
    val visible = _field.get(Attribute.VisibleAnnotations)
    val invisible = _field.get(Attribute.InvisibleAnnotations)
    return getAnnotations(classpath, visible, true) + getAnnotations(classpath, invisible, false)
  }
  set(value) {
    val (visible, invisible) = value.partition { it.isVisible }

    val constPool = _field.fieldInfo.constPool
    _field.set(
      Attribute.VisibleAnnotations,
      newAnnotation(constPool, visible, Attribute.VisibleAnnotations),
    )
    _field.set(
      Attribute.InvisibleAnnotations,
      newAnnotation(constPool, invisible, Attribute.InvisibleAnnotations),
    )

    propertyMetadata?.hasAnnotations = value.isNotEmpty()
  }

internal var MutableParameter.parameterAnnotations: List<AnnotationInstance>
  get() {
    val visible = behavior.get(Attribute.VisibleParameterAnnotations)
    val invisible = behavior.get(Attribute.InvisibleParameterAnnotations)
    return getAnnotations(classpath, visible, true, index) + getAnnotations(classpath, invisible, false, index)
  }
  set(value) {
    val (visible, invisible) = value.partition { it.isVisible }
    behavior.set(
      Attribute.VisibleParameterAnnotations,
      newParameterAnnotation(behavior, visible, Attribute.VisibleParameterAnnotations, index),
    )

    behavior.set(
      Attribute.InvisibleParameterAnnotations,
      newParameterAnnotation(behavior, invisible, Attribute.InvisibleParameterAnnotations, index),
    )

    parameterMetadata?.hasAnnotations = value.isNotEmpty()
  }

internal var MutableMethodReference.annotationDefaultValue: AnnotationInstance.Value?
  get() {
    return _method.get(Attribute.AnnotationDefaultValue)?.defaultValue?.toValue(classpath)
  }
  set(value) {
    _method.set(
      Attribute.AnnotationDefaultValue,
      value?.let {
        val newAttribute = AnnotationDefaultAttribute(_method.methodInfo.constPool)
        newAttribute.defaultValue = it.toMemberValue(_method.methodInfo.constPool)
        newAttribute
      },
    )
  }

internal fun getAnnotations(
  classpath: MutableClasspath,
  attributeInfo: AnnotationsAttribute?,
  visible: Boolean,
): List<AnnotationInstance> {
  return attributeInfo?.annotations
    ?.map { classpath[it, visible] }
    .orEmpty()
}

internal fun getAnnotations(
  classpath: MutableClasspath,
  attributeInfo: ParameterAnnotationsAttribute?,
  visible: Boolean,
  index: Int,
): List<AnnotationInstance> {
  return attributeInfo?.annotations
    ?.get(index)
    ?.map { classpath[it, visible] }
    .orEmpty()
}

internal fun <T : AttributeInfo> newAnnotation(
  constPool: ConstPool,
  annotations: List<AnnotationInstance>,
  attribute: Attribute<T>,
): AnnotationsAttribute {
  val annotationsAttribute = AnnotationsAttribute(constPool, attribute.key)
  annotationsAttribute.annotations = annotations.mapToArray { it._annotation }
  return annotationsAttribute
}

internal fun newParameterAnnotation(
  behavior: CtBehavior,
  annotations: List<AnnotationInstance>,
  attribute: Attribute<ParameterAnnotationsAttribute>,
  index: Int,
): ParameterAnnotationsAttribute {
  val existingAttribute = behavior.get(attribute)

  val (parameterAnnotationsAttribute, allAnnotations) = if (existingAttribute != null) {
    existingAttribute to existingAttribute.annotations
  } else {
    val newAttribute = ParameterAnnotationsAttribute(behavior.methodInfo.constPool, attribute.key)
    newAttribute to Array(Descriptor.numOfParameters(behavior.methodInfo.descriptor)) { emptyArray() }
  }

  allAnnotations[index] = annotations.mapToArray { it._annotation }
  parameterAnnotationsAttribute.annotations = allAnnotations
  return parameterAnnotationsAttribute
}
