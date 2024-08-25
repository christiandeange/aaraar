package sh.christian.aaraar.model.classeditor

import javassist.CtBehavior

internal sealed interface ParameterOwner {
  val classpath: MutableClasspath
  val behavior: CtBehavior
}

internal data class FromConstructor(
  val constructor: MutableConstructorReference,
) : ParameterOwner {
  override val classpath: MutableClasspath = constructor.classpath
  override val behavior: CtBehavior = constructor._constructor
}

internal data class FromMethod(
  val method: MutableMethodReference,
) : ParameterOwner {
  override val classpath: MutableClasspath = method.classpath
  override val behavior: CtBehavior = method._method
}
