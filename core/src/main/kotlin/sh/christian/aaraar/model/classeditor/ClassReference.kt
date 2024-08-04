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
  /** The major version of bytecode that this class definition targets. */
  var classMajorVersion: Int
    get() = _class.classFile.majorVersion
    set(value) {
      _class.classFile.majorVersion = value
    }

  /** The minor version of bytecode that this class definition targets, or `0` if not set. */
  var classMinorVersion: Int
    get() = _class.classFile.minorVersion
    set(value) {
      _class.classFile.minorVersion = value
    }

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
      _class.interfaces = value.mapToArray { it._class }
    }

  /** The set of constructors explicitly declared by this class. */
  var constructors: List<ConstructorReference>
    get() = _class.declaredConstructors.map { classpath[it] }
    set(value) {
      resolveDeltas(
        oldMembers = _class.declaredConstructors.toSet(),
        newMembers = value.mapTo(mutableSetOf()) { it._constructor },
        adder = _class::addConstructor,
        remover = _class::removeConstructor,
      )
    }

  /** The set of fields explicitly declared by this class. */
  var fields: List<FieldReference>
    get() = _class.declaredFields.map { classpath[it] }
    set(value) {
      resolveDeltas(
        oldMembers = _class.declaredFields.toSet(),
        newMembers = value.mapTo(mutableSetOf()) { it._field },
        adder = _class::addField,
        remover = _class::removeField,
      )
    }

  /** The set of methods explicitly declared by this class. */
  var methods: List<MethodReference>
    get() = _class.declaredMethods.map { classpath[it] }
    set(value) {
      resolveDeltas(
        oldMembers = _class.declaredMethods.toSet(),
        newMembers = value.mapTo(mutableSetOf()) { it._method },
        adder = _class::addMethod,
        remover = _class::removeMethod,
      )
    }

  /** Adds a new constructor explicitly declared by this class. */
  fun addConstructor(
    configure: ConstructorReference.() -> Unit = { },
  ): ConstructorReference {
    val newConstructor = CtConstructor(emptyArray(), _class)
    return classpath[newConstructor].also {
      it.modifiers = setOf(Modifier.PUBLIC)
      configure(it)
      constructors += it
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
      it.modifiers = setOf(Modifier.PUBLIC)
      configure(it)
      fields += it
    }
  }

  /** Adds a new method explicitly declared by this class. */
  fun addMethod(
    name: String,
    configure: MethodReference.() -> Unit = { },
  ): MethodReference {
    val newMethod = CtMethod(CtClass.voidType, name, emptyArray(), _class)
    return classpath[newMethod].also {
      it.modifiers = setOf(Modifier.PUBLIC)
      configure(it)
      methods += it
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

  /** Returns the declared field identified by this name, or `null` if none exists. */
  fun getField(name: String): FieldReference? {
    return fields.singleOrNull { it.name == name }
  }

  /** Returns the declared method identified by this name, or `null` if none exists. */
  fun getMethod(name: String): MethodReference? {
    return methods.singleOrNull { it.name == name }
  }

  /** Returns the bytecode associated with this class definition. */
  fun toBytecode(): ByteArray {
    if (!_class.isFrozen) {
      _class.classFile.compact()
    }
    return _class.toBytecode()
  }

  override fun equals(other: Any?): Boolean {
    if (other !is ClassReference) return false
    return _class == other._class
  }

  override fun hashCode(): Int {
    return qualifiedName.hashCode()
  }

  override fun toString(): String {
    return qualifiedName
  }

  private inline fun <T : Any> resolveDeltas(
    oldMembers: Set<T>,
    newMembers: Set<T>,
    crossinline adder: (T) -> Unit,
    crossinline remover: (T) -> Unit,
  ) {
    (oldMembers - newMembers).forEach(remover)
    (newMembers - oldMembers).forEach(adder)
  }
}
