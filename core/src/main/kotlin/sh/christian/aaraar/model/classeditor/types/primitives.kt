package sh.christian.aaraar.model.classeditor.types

import javassist.CtClass
import sh.christian.aaraar.model.classeditor.ClassReference
import sh.christian.aaraar.model.classeditor.Classpath
import sh.christian.aaraar.model.classeditor.MutableClassReference
import sh.christian.aaraar.model.classeditor.MutableClasspath

/** The primitive [Boolean] type. */
val Classpath.booleanType: ClassReference
  get() = get(Boolean::class.java)

/** The primitive [Char] type. */
val Classpath.charType: ClassReference
  get() = get(Char::class.java)

/** The primitive [Byte] type. */
val Classpath.byteType: ClassReference
  get() = get(Byte::class.java)

/** The primitive [Short] type. */
val Classpath.shortType: ClassReference
  get() = get(Short::class.java)

/** The primitive [Int] type. */
val Classpath.intType: ClassReference
  get() = get(Int::class.java)

/** The primitive [Long] type. */
val Classpath.longType: ClassReference
  get() = get(Long::class.java)

/** The primitive [Float] type. */
val Classpath.floatType: ClassReference
  get() = get(Float::class.java)

/** The primitive [Double] type. */
val Classpath.doubleType: ClassReference
  get() = get(Double::class.java)

/** The primitive [Void] type. */
val Classpath.voidType: ClassReference
  get() = get(Void::class.java)

/** The primitive [Boolean] type. */
val MutableClasspath.booleanType: MutableClassReference
  get() = get(Boolean::class.java)

/** The primitive [Char] type. */
val MutableClasspath.charType: MutableClassReference
  get() = get(Char::class.java)

/** The primitive [Byte] type. */
val MutableClasspath.byteType: MutableClassReference
  get() = get(Byte::class.java)

/** The primitive [Short] type. */
val MutableClasspath.shortType: MutableClassReference
  get() = get(Short::class.java)

/** The primitive [Int] type. */
val MutableClasspath.intType: MutableClassReference
  get() = get(Int::class.java)

/** The primitive [Long] type. */
val MutableClasspath.longType: MutableClassReference
  get() = get(Long::class.java)

/** The primitive [Float] type. */
val MutableClasspath.floatType: MutableClassReference
  get() = get(Float::class.java)

/** The primitive [Double] type. */
val MutableClasspath.doubleType: MutableClassReference
  get() = get(Double::class.java)

/** The primitive [Void] type. */
val MutableClasspath.voidType: MutableClassReference
  get() = get(CtClass.voidType)
