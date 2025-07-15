package sh.christian.aaraar.shading.pipeline

import sh.christian.aaraar.shading.impl.transform.Transformable
import sh.christian.aaraar.shading.impl.transform.config.ClassDelete
import sh.christian.aaraar.shading.impl.transform.jar.JarProcessor
import sh.christian.aaraar.shading.impl.transform.jar.JarProcessor.Companion.EXT_CLASS
import sh.christian.aaraar.shading.impl.transform.jar.JarProcessor.Result.DISCARD
import sh.christian.aaraar.shading.impl.transform.jar.JarProcessor.Result.KEEP

internal class ClassFileFilter(
  classDeletes: Set<String>,
) : JarProcessor {
  private val classDeletePatterns = classDeletes.map { ClassDelete(it) }

  override fun scan(struct: Transformable): JarProcessor.Result = process(struct)

  override fun process(struct: Transformable): JarProcessor.Result {
    if (classDeletePatterns.isEmpty() || !struct.name.endsWith(EXT_CLASS)) return KEEP

    return if (shouldDeletePath(struct.name.removeSuffix(EXT_CLASS))) {
      DISCARD
    } else {
      KEEP
    }
  }

  private fun shouldDeletePath(className: String): Boolean {
    return classDeletePatterns.any { it.matches(className) }
  }
}
