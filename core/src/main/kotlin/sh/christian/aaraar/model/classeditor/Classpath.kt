package sh.christian.aaraar.model.classeditor

import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtField
import javassist.CtMethod
import javassist.bytecode.annotation.Annotation
import sh.christian.aaraar.model.GenericJarArchive

/**
 * Represents a set of classes that are available at runtime.
 *
 * The full set of runtime classes is likely more than what is represented here, which is why calling [get] for any
 * unknown classes will return a virtual class definition that can still be modified and references as usual.
 * However, these virtual classes will be ignored when exporting the classpath via [toGenericJarArchive].
 */
class Classpath
internal constructor(
  internal val classPool: ClassPool,
  private val originalJar: GenericJarArchive,
) {
  private val classCache = mutableMapOf<String, ClassReference>()
  private val constructorCache = mutableMapOf<CtConstructor, ConstructorReference>()
  private val fieldCache = mutableMapOf<CtField, FieldReference>()
  private val methodCache = mutableMapOf<CtMethod, MethodReference>()
  private val annotationCache = mutableMapOf<Annotation, AnnotationInstance>()

  private val inputClasses = mutableSetOf<ClassReference>()

  /** The set of all input classes that will be packaged in this JAR. */
  val classes: Set<ClassReference>
    get() = inputClasses.toSet()

  /**
   * Adds and configures a new class entry in this classpath.
   * This class will be packaged in the resulting JAR file.
   */
  fun addClass(
    classname: String,
    configure: ClassReference.() -> Unit = { },
  ): ClassReference = synchronized(this) {
    return get(classname).also {
      configure(it)
      inputClasses += it
    }
  }

  /** Removes a class from being packaged in the resulting JAR file. */
  fun removeClass(classname: String) = synchronized(this) {
    inputClasses.removeIf { it.qualifiedName == classname }
  }

  /**
   * Adds an entire set of other classes from another classpath to this one.
   * Any classes you have defined in this classpath will be overwritten if also present in the other one.
   */
  fun addClasspath(other: Classpath) {
    inputClasses += other.inputClasses

    classCache += other.classCache
    constructorCache += other.constructorCache
    fieldCache += other.fieldCache
    methodCache += other.methodCache
    annotationCache += other.annotationCache
  }

  /** Returns the mutable class definition for the given class. */
  operator fun get(clazz: Class<*>): ClassReference = synchronized(this) {
    get(clazz.canonicalName)
  }

  /** Returns the mutable class definition for the given class name. */
  operator fun get(classname: String): ClassReference = synchronized(this) {
    val knownClass = classPool.getOrNull(classname)
    if (knownClass != null) {
      return get(knownClass)
    } else {
      return classCache.getOrPut(classname) {
        val newClass = classPool.makeClass(classname)
        ClassReference(this, newClass)
      }
    }
  }

  internal operator fun get(clazz: CtClass): ClassReference = synchronized(this) {
    classCache.getOrPut(clazz.name) { ClassReference(this, clazz) }
  }

  internal operator fun get(constructor: CtConstructor): ConstructorReference = synchronized(this) {
    constructorCache.getOrPut(constructor) { ConstructorReference(this, constructor) }
  }

  internal operator fun get(field: CtField): FieldReference = synchronized(this) {
    fieldCache.getOrPut(field) { FieldReference(this, field) }
  }

  internal operator fun get(method: CtMethod): MethodReference = synchronized(this) {
    methodCache.getOrPut(method) { MethodReference(this, method) }
  }

  internal operator fun get(annotation: Annotation): AnnotationInstance = synchronized(this) {
    annotationCache.getOrPut(annotation) { AnnotationInstance(this, annotation) }
  }

  /**
   * Modifies all class files to remove any information unrelated to a class's public API.
   */
  fun asApiJar() {
    removePrivateMembers()
    removeMethodBodies()
    removeKotlinMetadata()
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
   * Removes all [@Metadata][kotlin.Metadata] annotations from Kotlin classes.
   */
  fun removeKotlinMetadata() {
    inputClasses.forEach { clazz ->
      clazz.annotations = clazz.annotations.filterNot { it.qualifiedName == "kotlin.Metadata" }
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
   * Exports the modified classpath as a JAR representation.
   *
   * The resulting JAR reflects any changes made to class files, as well as containing all resource files from the
   * original JAR file that this classpath was constructed from. Once this method is called, modifying any
   * [ClassReference], [ConstructorReference], [FieldReference] or [MethodReference] definitions may throw an error.
   */
  fun toGenericJarArchive(): GenericJarArchive {
    val resources = originalJar.filterKeys { !it.endsWith(".class") }

    val classes = inputClasses.associate { clazz ->
      "${clazz.qualifiedName.replace('.', '/')}.class" to clazz.toBytecode()
    }

    return GenericJarArchive(classes + resources)
  }

  companion object {
    fun from(jarArchive: GenericJarArchive): Classpath {
      val cp = ClassPool()
      cp.appendSystemPath()
      val classpath = Classpath(cp, jarArchive)

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
