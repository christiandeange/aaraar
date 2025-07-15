package sh.christian.aaraar.shading.impl.jarjar.transform.jar

import sh.christian.aaraar.shading.impl.jarjar.transform.Transformable
import java.io.IOException

internal class JarProcessorChain(
  val processors: List<JarProcessor>,
) : JarProcessor {

  constructor(vararg processors: JarProcessor) : this(processors.toList())

  @Throws(IOException::class)
  override fun scan(struct: Transformable): JarProcessor.Result {
    return if (processors.any { it.scan(struct) == JarProcessor.Result.DISCARD }) {
      JarProcessor.Result.DISCARD
    } else {
      JarProcessor.Result.KEEP
    }
  }

  @Throws(IOException::class)
  override fun process(struct: Transformable): JarProcessor.Result {
    return if (processors.any { it.process(struct) == JarProcessor.Result.DISCARD }) {
      JarProcessor.Result.DISCARD
    } else {
      JarProcessor.Result.KEEP
    }
  }
}
