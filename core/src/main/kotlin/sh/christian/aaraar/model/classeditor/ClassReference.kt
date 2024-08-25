package sh.christian.aaraar.model.classeditor

/**
 * Represents a class definition.
 */
interface ClassReference {
  /** The major version of bytecode that this class definition targets. */
  val classMajorVersion: Int

  /** The minor version of bytecode that this class definition targets, or `0` if not set. */
  val classMinorVersion: Int

  /** The set of modifiers applied to the class definition. */
  val modifiers: Set<Modifier>

  /** This class's fully-qualified class name, including its package name and simple class name. */
  val qualifiedName: String

  /** The declared name of this specific class. */
  val simpleName: String

  /** The name of the package this class is defined in. */
  val packageName: String

  /** The set of annotations applied to this class definition. */
  val annotations: List<AnnotationInstance>

  /** The supertype of this class, or `null` if none defined. */
  val superclass: ClassReference?

  /**
   * If this is a class, these are the set of interface types implemented by this class.
   * If this is an interface, these are the interfaces extended by this interface.
   */
  val interfaces: List<ClassReference>

  /** The set of constructors explicitly declared by this class. */
  val constructors: List<ConstructorReference>

  /** The set of fields explicitly declared by this class. */
  val fields: List<FieldReference>

  /** The set of methods explicitly declared by this class. */
  val methods: List<MethodReference>

  /** Returns the declared field identified by this name, or `null` if none exists. */
  fun getField(name: String): FieldReference?

  /** Returns the declared method identified by this name, or `null` if none exists. */
  fun getMethod(name: String): MethodReference?

  /** Returns the bytecode associated with this class definition. */
  fun toBytecode(): ByteArray
}
