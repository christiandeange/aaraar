package sh.christian.aaraar.model.classeditor.metadata

import kotlinx.metadata.ClassName
import kotlinx.metadata.KmConstructor
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmProperty
import kotlinx.metadata.Visibility
import kotlinx.metadata.jvm.fieldSignature
import kotlinx.metadata.jvm.getterSignature
import kotlinx.metadata.jvm.setterSignature
import kotlinx.metadata.jvm.signature
import sh.christian.aaraar.model.classeditor.ConstructorSignature
import sh.christian.aaraar.model.classeditor.FieldSignature
import sh.christian.aaraar.model.classeditor.MethodSignature
import sh.christian.aaraar.model.classeditor.Modifier
import sh.christian.aaraar.model.classeditor.Signature

internal fun String.toClassName(): ClassName {
  return this.replace(".", "/")
}

internal fun ClassName.toQualifiedName(): String {
  return this.replace("/", ".")
}

internal fun KmConstructor.signature(): ConstructorSignature {
  return ConstructorSignature(signature!!.descriptor)
}

internal fun KmFunction.signature(): MethodSignature {
  return MethodSignature(name, signature!!.descriptor)
}

internal fun KmProperty.fieldSignature(): Signature? {
  return fieldSignature?.let { FieldSignature(it.name, it.descriptor) }
}

internal fun KmProperty.getterSignature(): Signature? {
  return getterSignature?.let { MethodSignature(it.name, it.descriptor) }
}

internal fun KmProperty.setterSignature(): Signature? {
  return setterSignature?.let { MethodSignature(it.name, it.descriptor) }
}

internal fun KmProperty.signatures(): List<Signature> {
  return listOfNotNull(fieldSignature(), getterSignature(), setterSignature())
}

internal fun Set<Modifier>.toVisibility(): Visibility {
  return when {
    Modifier.PUBLIC in this -> Visibility.PUBLIC
    Modifier.PROTECTED in this -> Visibility.PROTECTED
    Modifier.PRIVATE in this -> Visibility.PRIVATE
    // Assume internal if no visibility modifiers are present.
    else -> Visibility.INTERNAL
  }
}
