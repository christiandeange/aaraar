package sh.christian.aaraar.model.classeditor

import sh.christian.aaraar.model.GenericJarArchive

/**
 * Represents a set of classes that are available at runtime.
 *
 * The full set of runtime classes is likely more than what is represented here, which is why calling [get] for any
 * unknown classes will return a virtual class definition that can still be referenced as usual.
 * However, these virtual classes will be ignored when exporting the classpath via [toGenericJarArchive], and won't
 * include all the information (like supertypes, declared functions, etc) that the real class would.
 */
interface Classpath {
  /** The set of all input classes that will be packaged in this JAR. */
  val classes: Set<ClassReference>

  /** Returns the class definition for the given class, or throws if one does not exist. */
  operator fun get(clazz: Class<*>): ClassReference

  /**
   * Returns the class definition for the given class name, or returns a virtual definition if one does not exist.
   * Virtual definitions won't include all the information (like supertypes, declared functions, etc) that the real
   * class would, they are simply a placeholder for referencing a type from another compilation unit.
   */
  operator fun get(className: String): ClassReference

  /** Returns the class definition for the given class name, or `null` if none exists. */
  fun getOrNull(className: String): ClassReference?

  /** Returns the classpath as a JAR representation. */
  fun toGenericJarArchive(): GenericJarArchive

  companion object {
    fun from(jarArchive: GenericJarArchive): Classpath {
      return MutableClasspath.from(jarArchive)
    }
  }
}
