package sh.christian.aaraar.model.classeditor.types

import sh.christian.aaraar.model.classeditor.ClassReference
import sh.christian.aaraar.model.classeditor.Classpath
import sh.christian.aaraar.model.classeditor.MutableClassReference
import sh.christian.aaraar.model.classeditor.MutableClasspath

/** The platform [Class] type. */
val Classpath.classType: ClassReference
  get() = this["java.lang.Class"]

/** The platform [Object] type. */
val Classpath.objectType: ClassReference
  get() = this["java.lang.Object"]

/** The platform [String] type. */
val Classpath.stringType: ClassReference
  get() = this["java.lang.String"]

/** The platform [Class] type. */
val MutableClasspath.classType: MutableClassReference
  get() = this["java.lang.Class"]

/** The platform [Object] type. */
val MutableClasspath.objectType: MutableClassReference
  get() = this["java.lang.Object"]

/** The platform [String] type. */
val MutableClasspath.stringType: MutableClassReference
  get() = this["java.lang.String"]
