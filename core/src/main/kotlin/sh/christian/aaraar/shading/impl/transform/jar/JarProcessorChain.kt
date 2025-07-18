package sh.christian.aaraar.shading.impl.transform.jar

import sh.christian.aaraar.shading.impl.transform.Transformable
import java.io.IOException

internal class JarProcessorChain(
  val processors: List<JarProcessor>,
) : JarProcessor {

  constructor(vararg processors: JarProcessor) : this(processors.toList())

  @Throws(IOException::class)
  override fun process(struct: Transformable): JarProcessor.Result {
    return if (processors.any { it.process(struct) == JarProcessor.Result.DISCARD }) {
      JarProcessor.Result.DISCARD
    } else {
      JarProcessor.Result.KEEP
    }
  }
}
