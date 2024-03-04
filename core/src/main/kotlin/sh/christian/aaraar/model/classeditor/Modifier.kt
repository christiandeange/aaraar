package sh.christian.aaraar.model.classeditor

import javassist.Modifier as JModifier

/**
 * A modifier keyword that may be applied to a class, member, or parameter.
 */
enum class Modifier {
  PUBLIC,
  PROTECTED,
  PRIVATE,
  ABSTRACT,
  STATIC,
  FINAL,
  TRANSIENT,
  VOLATILE,
  SYNCHRONIZED,
  NATIVE,
  STRICTFP,
  VARARG,
  ANNOTATION,
  INTERFACE,
  ENUM,
  ;

  override fun toString(): String {
    return name.lowercase()
  }

  internal companion object {
    fun fromModifiers(modifiers: Int): Set<Modifier> {
      return setOfNotNull(
        PUBLIC.takeIf { JModifier.isPublic(modifiers) },
        PROTECTED.takeIf { JModifier.isProtected(modifiers) },
        PRIVATE.takeIf { JModifier.isPrivate(modifiers) },
        ABSTRACT.takeIf { JModifier.isAbstract(modifiers) },
        STATIC.takeIf { JModifier.isStatic(modifiers) },
        FINAL.takeIf { JModifier.isFinal(modifiers) },
        TRANSIENT.takeIf { JModifier.isTransient(modifiers) },
        VOLATILE.takeIf { JModifier.isVolatile(modifiers) },
        SYNCHRONIZED.takeIf { JModifier.isSynchronized(modifiers) },
        NATIVE.takeIf { JModifier.isNative(modifiers) },
        STRICTFP.takeIf { JModifier.isStrict(modifiers) },
        VARARG.takeIf { JModifier.isVarArgs(modifiers) },
        ANNOTATION.takeIf { JModifier.isAnnotation(modifiers) },
        INTERFACE.takeIf { JModifier.isInterface(modifiers) },
        ENUM.takeIf { JModifier.isEnum(modifiers) },
      )
    }

    internal fun Set<Modifier>.toModifiers(): Int {
      return map {
        when (it) {
          PUBLIC -> JModifier.PUBLIC
          PROTECTED -> JModifier.PROTECTED
          PRIVATE -> JModifier.PRIVATE
          ABSTRACT -> JModifier.ABSTRACT
          STATIC -> JModifier.STATIC
          FINAL -> JModifier.FINAL
          TRANSIENT -> JModifier.TRANSIENT
          VOLATILE -> JModifier.VOLATILE
          SYNCHRONIZED -> JModifier.SYNCHRONIZED
          NATIVE -> JModifier.NATIVE
          STRICTFP -> JModifier.STRICT
          VARARG -> JModifier.VARARGS
          ANNOTATION -> JModifier.ANNOTATION
          INTERFACE -> JModifier.INTERFACE
          ENUM -> JModifier.ENUM
        }
      }.fold(0, Int::or)
    }
  }
}
