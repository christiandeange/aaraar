package sh.christian.aaraar.model.classeditor

import javassist.Modifier as JModifier

/**
 * A modifier keyword that may be applied to a class, member, or parameter.
 */
enum class Modifier {
  PUBLIC,
  PRIVATE,
  PROTECTED,
  STATIC,
  FINAL,
  SYNCHRONIZED,
  VOLATILE,
  VARARGS,
  TRANSIENT,
  NATIVE,
  INTERFACE,
  ABSTRACT,
  STRICT,
  ANNOTATION,
  ENUM,
  ;

  internal companion object {
    fun fromModifiers(modifiers: Int): Set<Modifier> {
      return setOfNotNull(
        PUBLIC.takeIf { JModifier.isPublic(modifiers) },
        PRIVATE.takeIf { JModifier.isPrivate(modifiers) },
        PROTECTED.takeIf { JModifier.isProtected(modifiers) },
        STATIC.takeIf { JModifier.isStatic(modifiers) },
        FINAL.takeIf { JModifier.isFinal(modifiers) },
        SYNCHRONIZED.takeIf { JModifier.isSynchronized(modifiers) },
        VOLATILE.takeIf { JModifier.isVolatile(modifiers) },
        VARARGS.takeIf { JModifier.isVarArgs(modifiers) },
        TRANSIENT.takeIf { JModifier.isTransient(modifiers) },
        NATIVE.takeIf { JModifier.isNative(modifiers) },
        INTERFACE.takeIf { JModifier.isInterface(modifiers) },
        ABSTRACT.takeIf { JModifier.isAbstract(modifiers) },
        STRICT.takeIf { JModifier.isStrict(modifiers) },
        ANNOTATION.takeIf { JModifier.isAnnotation(modifiers) },
        ENUM.takeIf { JModifier.isEnum(modifiers) },
      )
    }

    internal fun Set<Modifier>.toModifiers(): Int {
      return map {
        when (it) {
          PUBLIC -> JModifier.PUBLIC
          PRIVATE -> JModifier.PRIVATE
          PROTECTED -> JModifier.PROTECTED
          STATIC -> JModifier.STATIC
          FINAL -> JModifier.FINAL
          SYNCHRONIZED -> JModifier.SYNCHRONIZED
          VOLATILE -> JModifier.VOLATILE
          VARARGS -> JModifier.VARARGS
          TRANSIENT -> JModifier.TRANSIENT
          NATIVE -> JModifier.NATIVE
          INTERFACE -> JModifier.INTERFACE
          ABSTRACT -> JModifier.ABSTRACT
          STRICT -> JModifier.STRICT
          ANNOTATION -> JModifier.ANNOTATION
          ENUM -> JModifier.ENUM
        }
      }.fold(0, Int::or)
    }
  }
}
