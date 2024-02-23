package sh.christian.aaraar.model.classeditor

import javassist.CtClass
import javassist.CtConstructor
import javassist.CtField
import javassist.CtMethod
import sh.christian.aaraar.model.classeditor.Modifier.Companion.toModifiers

/**
 * Represents a class definition.
 *
 * This representation is mutable, to allow changing properties of the class.
 */
class ClassReference
internal constructor(
  internal val classpath: Classpath,
  internal val _class: CtClass,
) {
  /** The set of modifiers applied to the class definition. */
  var modifiers: Set<Modifier>
    get() = Modifier.fromModifiers(_class.modifiers)
    set(value) {
      _class.modifiers = value.toModifiers()
    }

  /** This class's fully-qualified classname, including its package name and simple class name. */
  var qualifiedName: String by _class::name

  /** The declared name of this specific class. */
  var simpleName: String
    get() = _class.simpleName
    set(value) {
      qualifiedName = "$packageName.$value"
    }

  /** The name of the package this class is defined in. */
  var packageName: String
    get() = _class.packageName
    set(value) {
      qualifiedName = "$value.$simpleName"
    }

  /** The set of annotations applied to this class definition. */
  var annotations: List<AnnotationInstance> by ::classAnnotations

  /** The supertype of this class, or `null` if none defined. */
  var superclass: ClassReference?
    get() = _class.superclass?.let { classpath[it] }
    set(value) {
      _class.superclass = value?._class
    }

  /**
   * If this is a class, these are the set of interface types implemented by this class.
   * If this is an interface, these are the interfaces extended by this interface.
   */
  var interfaces: List<ClassReference>
    get() = _class.interfaces.map { classpath[it] }
    set(value) {
      _class.interfaces = value.map { it._class }.toTypedArray()
    }

  /** The set of constructors explicitly declared by this class. */
  var constructors: List<ConstructorReference>
    get() = _class.declaredConstructors.map { classpath[it] }
    set(value) {
      _class.declaredConstructors.forEach { _class.removeConstructor(it) }
      value.forEach { _class.addConstructor(it._constructor) }
    }

  /** The set of fields explicitly declared by this class. */
  var fields: List<FieldReference>
    get() = _class.declaredFields.map { classpath[it] }
    set(value) {
      _class.declaredFields.forEach { _class.removeField(it) }
      value.forEach { _class.addField(it._field) }
    }

  /** The set of methods explicitly declared by this class. */
  var methods: List<MethodReference>
    get() = _class.declaredMethods.map { classpath[it] }
    set(value) {
      _class.declaredMethods.forEach { _class.removeMethod(it) }
      value.forEach { _class.addMethod(it._method) }
    }

  init {
    if (!_class.isFrozen) {
      modifiers = setOf(Modifier.PUBLIC)
    }
  }

  /** Adds a new constructor explicitly declared by this class. */
  fun addConstructor(
    configure: ConstructorReference.() -> Unit = { },
  ): ConstructorReference {
    val newConstructor = CtConstructor(emptyArray(), _class)
    return classpath[newConstructor].also {
      constructors += it
      configure(it)
    }
  }

  /** Adds a new field explicitly declared by this class. */
  fun addField(
    name: String,
    type: ClassReference,
    configure: FieldReference.() -> Unit = { },
  ): FieldReference {
    val newField = CtField(type._class, name, _class)
    return classpath[newField].also {
      fields += it
      configure(it)
    }
  }

  /** Adds a new method explicitly declared by this class. */
  fun addMethod(
    name: String,
    configure: MethodReference.() -> Unit = { },
  ): MethodReference {
    val newMethod = CtMethod(CtClass.voidType, name, emptyArray(), _class)
    return classpath[newMethod].also {
      methods += it
      configure(it)
    }
  }

  /** Creates a new annotation usage instance for use *only* in this class. */
  fun annotationInstance(
    type: ClassReference,
    configure: AnnotationInstance.Builder.() -> Unit = { },
  ): AnnotationInstance {
    return AnnotationInstance.Builder(type)
      .apply(configure)
      .forUseIn(this)
  }

  /** Returns the declared constructor identified by this name, or `null` if none exists. */
  fun getConstructor(name: String): ConstructorReference? {
    return constructors.singleOrNull { it.name == name }
  }

  /** Returns the declared field identified by this name, or `null` if none exists. */
  fun getField(name: String): FieldReference? {
    return fields.singleOrNull { it.name == name }
  }

  /** Returns the declared method identified by this name, or `null` if none exists. */
  fun getMethod(name: String): MethodReference? {
    return methods.singleOrNull { it.name == name }
  }

  override fun toString(): String {
    return qualifiedName
  }

  internal fun toBytecode(): ByteArray {
    _class.classFile.compact()
    return _class.toBytecode()
  }
}
