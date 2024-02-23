package sh.christian.aaraar.model.classeditor.types

import javassist.CtClass
import sh.christian.aaraar.model.classeditor.ClassReference
import sh.christian.aaraar.model.classeditor.Classpath

/** The primitive [Boolean] type. */
val Classpath.booleanType: ClassReference
  get() = this[CtClass.booleanType]

/** The primitive [Char] type. */
val Classpath.charType: ClassReference
  get() = this[CtClass.charType]

/** The primitive [Byte] type. */
val Classpath.byteType: ClassReference
  get() = this[CtClass.byteType]

/** The primitive [Short] type. */
val Classpath.shortType: ClassReference
  get() = this[CtClass.shortType]

/** The primitive [Int] type. */
val Classpath.intType: ClassReference
  get() = this[CtClass.intType]

/** The primitive [Long] type. */
val Classpath.longType: ClassReference
  get() = this[CtClass.longType]

/** The primitive [Float] type. */
val Classpath.floatType: ClassReference
  get() = this[CtClass.floatType]

/** The primitive [Double] type. */
val Classpath.doubleType: ClassReference
  get() = this[CtClass.doubleType]

/** The primitive [Void] type. */
val Classpath.voidType: ClassReference
  get() = this[CtClass.voidType]
