package sh.christian.aaraar.model.classeditor.metadata

import kotlinx.metadata.ClassName
import sh.christian.aaraar.model.classeditor.ClassReference

/** Converts the [ClassName] notation to its associated [ClassReference.qualifiedName] format. */
fun ClassName.toQualifiedName(): String {
  return this.replace("/", ".")
}
