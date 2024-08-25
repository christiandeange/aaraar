package sh.christian.aaraar.model.classeditor

import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtField
import javassist.CtMethod
import javassist.bytecode.annotation.Annotation
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType
import sh.christian.aaraar.model.GenericJarArchive
import sh.christian.aaraar.model.classeditor.metadata.toClassName

/**
 * Represents a set of classes that are available at runtime.
 *
 * The full set of runtime classes is likely more than what is represented here, which is why calling [get] for any
 * unknown classes will return a virtual class definition that can still be referenced as usual.
 * However, these virtual classes will be ignored when exporting the classpath via [toGenericJarArchive], and won't
 * include all the information (like supertypes, declared functions, etc) that the real class would.
 */
class MutableClasspath
internal constructor(
  private val classPool: ClassPool,
  private val originalJar: GenericJarArchive,
) : Classpath {
  private val kmTypeCache = mutableMapOf<String, KmType>()
  private val classCache = mutableMapOf<String, MutableClassReference>()
  private val constructorCache = mutableMapOf<CtConstructor, MutableConstructorReference>()
  private val fieldCache = mutableMapOf<CtField, MutableFieldReference>()
  private val methodCache = mutableMapOf<CtMethod, MutableMethodReference>()
  private val annotationCache = mutableMapOf<Annotation, AnnotationInstance>()

  private val inputClasses = mutableSetOf<MutableClassReference>()

  override val classes: Set<MutableClassReference>
    get() = inputClasses.toSet()

  override operator fun get(clazz: Class<*>): MutableClassReference = synchronized(this) {
    get(clazz.canonicalName)
  }

  override operator fun get(className: String): MutableClassReference = synchronized(this) {
    return getOrCreate(className)
  }

  override fun getOrNull(className: String): MutableClassReference? = synchronized(this) {
    classPool.getOrNull(className)?.let { ctClass -> get(ctClass) }
  }

  fun getOrCreate(className: String): MutableClassReference = synchronized(this) {
    val knownClass = classPool.getOrNull(className)
    if (knownClass != null) {
      return get(knownClass)
    } else {
      cacheKmType(className)
      return classCache.getOrPut(className) {
        val newClass = classPool.makeClass(className)
        MutableClassReference(this, newClass)
      }
    }
  }

  /**
   * Adds and configures a new class entry in this classpath.
   * This class will be packaged in the resulting JAR file.
   */
  fun addClass(
    className: String,
    configure: MutableClassReference.() -> Unit = { },
  ): MutableClassReference = synchronized(this) {
    return getOrCreate(className).also {
      it.modifiers = setOf(Modifier.PUBLIC)
      configure(it)
      inputClasses += it
    }
  }

  /** Removes a class from being packaged in the resulting JAR file. */
  fun removeClass(className: String) = synchronized(this) {
    inputClasses.removeIf { it.qualifiedName == className }
  }

  /**
   * Adds an entire set of other classes from another classpath to this one.
   * Any classes you have defined in this classpath will be overwritten if also present in the other one.
   * If [addAsInput] is `true`, the classes from the other classpath will also be included in the resulting JAR file.
   */
  fun addClasspath(
    other: MutableClasspath,
    addAsInput: Boolean = true,
  ) {
    if (addAsInput) {
      inputClasses += other.inputClasses
    }

    classCache += other.classCache
    constructorCache += other.constructorCache
    fieldCache += other.fieldCache
    methodCache += other.methodCache
    annotationCache += other.annotationCache

    other.classes.forEach { cacheKmType(it.qualifiedName) }
  }

  internal operator fun get(clazz: CtClass): MutableClassReference = synchronized(this) {
    cacheKmType(clazz.name)
    classCache.getOrPut(clazz.name) { MutableClassReference(this, clazz) }
  }

  internal operator fun get(constructor: CtConstructor): MutableConstructorReference = synchronized(this) {
    constructorCache.getOrPut(constructor) { MutableConstructorReference(this, constructor) }
  }

  internal operator fun get(field: CtField): MutableFieldReference = synchronized(this) {
    fieldCache.getOrPut(field) { MutableFieldReference(this, field) }
  }

  internal operator fun get(method: CtMethod): MutableMethodReference = synchronized(this) {
    methodCache.getOrPut(method) { MutableMethodReference(this, method) }
  }

  internal operator fun get(
    annotation: Annotation,
    visible: Boolean,
  ): AnnotationInstance = synchronized(this) {
    annotationCache.getOrPut(annotation) { AnnotationInstance(this, annotation, visible) }
  }

  internal fun kmType(className: String): KmType = synchronized(this) {
    return cacheKmType(className)
  }

  internal fun kmClassifier(className: String): KmClassifier = synchronized(this) {
    return kmType(className).classifier
  }

  /**
   * Modifies all class files to remove any information unrelated to a class's public API.
   */
  fun asApiJar() {
    removePrivateMembers()
    removeMethodBodies()
  }

  /**
   * Modifies all class files to remove method body bytecode.
   */
  fun removeMethodBodies() {
    inputClasses.forEach { clazz ->
      clazz.constructors.forEach {
        it.removeConstructorBody()
      }
      clazz.methods.forEach {
        it.removeMethodBody()
      }
      clazz.fields.forEach {
        it.removeConstantInitializer()
      }
    }
  }

  /**
   * Modifies all class files to strip private members.
   */
  fun removePrivateMembers() {
    inputClasses.removeIf { Modifier.PRIVATE in it.modifiers }
    inputClasses.forEach { clazz ->
      clazz.constructors = clazz.constructors.filterNot { Modifier.PRIVATE in it.modifiers }
      clazz.methods = clazz.methods.filterNot { Modifier.PRIVATE in it.modifiers }
      clazz.fields = clazz.fields.filterNot { Modifier.PRIVATE in it.modifiers }
    }
  }

  /**
   * Returns the modified classpath as a JAR representation.
   *
   * The resulting JAR reflects any changes made to class files, as well as containing all resource files from the
   * original JAR file that this classpath was constructed from. Once this method is called, modifying any
   * [MutableClassReference], [MutableConstructorReference], [MutableFieldReference] or [MutableMethodReference]
   * definitions may throw an error.
   */
  override fun toGenericJarArchive(): GenericJarArchive {
    val resources = originalJar.filterKeys { !it.endsWith(".class") }

    inputClasses.forEach { it.finalizeClass() }

    val classes = inputClasses
      .associate { clazz ->
        val fileName = "${clazz.qualifiedName.replace('.', '/')}.class"
        val bytecode = if (Modifier.ENUM in clazz.modifiers || clazz.superclass?.qualifiedName == "java.lang.Enum") {
          originalJar[fileName] ?: byteArrayOf()
        } else {
          clazz.toBytecode()
        }
        fileName to bytecode
      }
      .filterValues { it.isNotEmpty() }

    return GenericJarArchive(classes + resources)
  }

  private fun cacheKmType(className: String): KmType {
    val kotlinTypeName = when (className) {
      "java.lang.Boolean", "boolean" -> "kotlin.Boolean"
      "java.lang.Character", "char" -> "kotlin.Char"
      "java.lang.Byte", "byte" -> "kotlin.Byte"
      "java.lang.Short", "short" -> "kotlin.Short"
      "java.lang.Integer", "int" -> "kotlin.Int"
      "java.lang.Long", "long" -> "kotlin.Long"
      "java.lang.Float", "float" -> "kotlin.Float"
      "java.lang.Double", "double" -> "kotlin.Double"
      "java.lang.Void", "void" -> "kotlin.Unit"
      "java.lang.Object" -> "kotlin.Any"
      "java.lang.String" -> "kotlin.String"
      else -> className
    }

    return kmTypeCache.getOrPut(kotlinTypeName) {
      KmType().apply { classifier = KmClassifier.Class(kotlinTypeName.toClassName()) }
    }
  }

  companion object {
    fun from(jarArchive: GenericJarArchive): MutableClasspath {
      val cp = ClassPool()
      cp.appendSystemPath()
      val classpath = MutableClasspath(cp, jarArchive)

      classpath.inputClasses.addAll(
        jarArchive
          // Only look for class files
          .filterKeys { it.endsWith(".class") }
          // Parse the class file into the Javassist representation
          .map { (_, contents) -> cp.makeClass(contents.inputStream()) }
          // Prime the class name to reference cache.
          .map { classpath[it] }
      )

      return classpath
    }
  }
}
