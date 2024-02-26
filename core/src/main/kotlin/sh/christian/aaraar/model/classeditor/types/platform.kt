package sh.christian.aaraar.model.classeditor.types

import sh.christian.aaraar.model.classeditor.ClassReference
import sh.christian.aaraar.model.classeditor.Classpath

/** The platform [Class] type. */
val Classpath.classType: ClassReference
  get() = this["java.lang.Class"]

/** The platform [Object] type. */
val Classpath.objectType: ClassReference
  get() = this["java.lang.Object"]

/** The platform [String] type. */
val Classpath.stringType: ClassReference
  get() = this["java.lang.String"]
