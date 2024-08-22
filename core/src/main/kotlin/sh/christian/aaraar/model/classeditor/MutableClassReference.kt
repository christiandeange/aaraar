package sh.christian.aaraar.model.classeditor

import javassist.CtClass
import javassist.CtConstructor
import javassist.CtField
import javassist.CtMethod
import kotlinx.metadata.jvm.KotlinClassMetadata
import sh.christian.aaraar.model.classeditor.AnnotationInstance.Value.ArrayValue
import sh.christian.aaraar.model.classeditor.AnnotationInstance.Value.IntegerValue
import sh.christian.aaraar.model.classeditor.AnnotationInstance.Value.StringValue
import sh.christian.aaraar.model.classeditor.Modifier.Companion.toModifiers

/**
 * Represents a class definition.
 *
 * This representation is mutable, to allow changing properties of the class.
 */
class MutableClassReference
internal constructor(
  internal val classpath: MutableClasspath,
  internal val _class: CtClass,
) : ClassReference {
  override var classMajorVersion: Int
    get() = _class.classFile.majorVersion
    set(value) {
      _class.classFile.majorVersion = value
    }

  override var classMinorVersion: Int
    get() = _class.classFile.minorVersion
    set(value) {
      _class.classFile.minorVersion = value
    }

  override var modifiers: Set<Modifier>
    get() = Modifier.fromModifiers(_class.modifiers)
    set(value) {
      _class.modifiers = value.toModifiers()
    }

  override var qualifiedName: String by _class::name

  override var simpleName: String
    get() = _class.simpleName
    set(value) {
      qualifiedName = "$packageName.$value"
    }

  override var packageName: String
    get() = _class.packageName
    set(value) {
      qualifiedName = "$value.$simpleName"
    }

  override val kotlinMetadata: KotlinClassMetadata.Class? =
    (_class.getAnnotation(Metadata::class.java) as? Metadata)?.let { annotation ->
      KotlinClassMetadata.readStrict(annotation) as? KotlinClassMetadata.Class
    }

  override var annotations: List<AnnotationInstance> by ::classAnnotations

  override var superclass: MutableClassReference?
    get() = _class.superclass?.let { classpath[it] }
    set(value) {
      _class.superclass = value?._class
    }

  override var interfaces: List<MutableClassReference>
    get() = _class.interfaces.map { classpath[it] }
    set(value) {
      _class.interfaces = value.mapToArray { it._class }
    }

  override var constructors: List<MutableConstructorReference>
    get() = _class.declaredConstructors.map { classpath[it] }
    set(value) {
      resolveDeltas(
        oldMembers = _class.declaredConstructors.toSet(),
        newMembers = value.mapTo(mutableSetOf()) { it._constructor },
        adder = _class::addConstructor,
        remover = _class::removeConstructor,
      )
    }

  override var fields: List<MutableFieldReference>
    get() = _class.declaredFields.map { classpath[it] }
    set(value) {
      resolveDeltas(
        oldMembers = _class.declaredFields.toSet(),
        newMembers = value.mapTo(mutableSetOf()) { it._field },
        adder = _class::addField,
        remover = _class::removeField,
      )
    }

  override var methods: List<MutableMethodReference>
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
    configure: MutableConstructorReference.() -> Unit = { },
  ): MutableConstructorReference {
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
    type: MutableClassReference,
    configure: MutableFieldReference.() -> Unit = { },
  ): MutableFieldReference {
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
    configure: MutableMethodReference.() -> Unit = { },
  ): MutableMethodReference {
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

  override fun getField(name: String): MutableFieldReference? {
    return fields.singleOrNull { it.name == name }
  }

  override fun getMethod(name: String): MutableMethodReference? {
    return methods.singleOrNull { it.name == name }
  }

  override fun toBytecode(): ByteArray {
    updateKotlinMetadata()

    if (!_class.isFrozen) {
      _class.classFile.compact()
    }
    return _class.toBytecode()
  }

  override fun equals(other: Any?): Boolean {
    if (other !is MutableClassReference) return false
    return _class == other._class
  }

  override fun hashCode(): Int {
    return qualifiedName.hashCode()
  }

  override fun toString(): String {
    return qualifiedName
  }

  private fun updateKotlinMetadata() {
    val annotationName = "kotlin.Metadata"
    val existingMetadataAnnotations = annotations.filter { it.type.qualifiedName == annotationName }.toSet()

    if (kotlinMetadata != null && existingMetadataAnnotations.isNotEmpty()) {
      // Metadata existed on the class before, and the annotation hasn't been removed
      val current = kotlinMetadata.write()
      val default = Metadata()

      @Suppress("KotlinConstantConditions")
      annotations = annotations - existingMetadataAnnotations + annotationInstance(classpath[annotationName]) {
        if (default.kind != current.kind) {
          addValue("k", IntegerValue(current.kind))
        }
        if (!default.metadataVersion.contentEquals(current.metadataVersion)) {
          addValue("mv", ArrayValue(current.metadataVersion.map(::IntegerValue)))
        }
        if (!default.data1.contentEquals(current.data1)) {
          addValue("d1", ArrayValue(current.data1.map(::StringValue)))
        }
        if (!default.data2.contentEquals(current.data2)) {
          addValue("d2", ArrayValue(current.data2.map(::StringValue)))
        }
        if (default.extraString != current.extraString) {
          addValue("xs", StringValue(current.extraString))
        }
        if (default.packageName != current.packageName) {
          addValue("pn", StringValue(current.packageName))
        }
        if (default.extraInt != current.extraInt) {
          addValue("xi", IntegerValue(current.extraInt))
        }
      }
    }
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
